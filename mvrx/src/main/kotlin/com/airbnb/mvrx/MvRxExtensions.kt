package com.airbnb.mvrx

import android.app.Activity
import android.view.View
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import io.reactivex.internal.disposables.EmptyDisposable
import java.lang.IllegalArgumentException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Gets or creates a ViewModel scoped to this Fragment. You will get the same instance every time for this Fragment, even
 * through rotation, or other configuration changes.
 *
 * If the ViewModel has additional dependencies, implement [MvRxViewModelFactory] in its companion object.
 * You will be given the initial state as well as a FragmentActivity with which you can access other dependencies to
 * pass to the ViewModel's constructor.
 *
 * MvRx will also handle persistence across process restarts. Refer to [PersistState] for more info.
 *
 * Use [keyFactory] if you have multiple ViewModels of the same class in the same scope.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.fragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this, keyFactory) { key ->
    val viewModel = MvRxViewModelProvider.get(viewModelClass.java, S::class.java, FragmentViewModelContext(this.requireActivity(), _fragmentArgsProvider(), this, key), key)
    viewModel to viewModel.subscribe(this@fragmentViewModel, subscriber = { postInvalidate() })
}

/**
 * [activityViewModel] except it will throw [IllegalStateException] if the ViewModel doesn't already exist.
 * Use this for screens in the middle of a flow that cannot reasonably be an entrypoint to the flow.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, S : MvRxState> T.existingViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this, keyFactory) { key ->
    val factory = MvRxFactory { error("ViewModel for ${requireActivity()}[${key}] does not exist yet!") }
    val viewModel = ViewModelProviders.of(requireActivity(), factory).get(key, viewModelClass.java)
    viewModel to viewModel.subscribe(this@existingViewModel, subscriber = { postInvalidate() })
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.activityViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this, keyFactory) { key ->
    require(requireActivity() is MvRxViewModelStoreOwner) { "Your Activity must be a MvRxViewModelStoreOwner!" }
    val viewModel = MvRxViewModelProvider.get(viewModelClass.java, S::class.java, ActivityViewModelContext(requireActivity(), _activityArgsProvider(key), key), key)
    viewModel to viewModel.subscribe(this@activityViewModel, subscriber = { postInvalidate() })
}

inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.viewViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : View, T : StatefulView {
    val viewLifecycleOwner = ViewLifecycleOwner(this)
    return lifecycleAwareLazy(viewLifecycleOwner, keyFactory) { key ->
        val fragment = viewLifecycleOwner.fragment
        val activity = requireNotNull(viewLifecycleOwner.activity) { "View (${this@viewViewModel}) must be attached to an Activity." }
        val storeOwner = activity as? MvRxViewModelStoreOwner ?: error("View's activity must be a MvRxViewModelStoreOwner")

        val context = ViewViewModelContext(activity, fragment, fragment?._fragmentArgsProvider(), this, storeOwner, key)
        val viewModel = MvRxViewModelProvider.get(viewModelClass.java, S::class.java, context, key)
        viewModel to viewModel.subscribe(viewLifecycleOwner, subscriber = { postOnInvalidate() })
    }
}

/**
 * Gets or creates a Fragment-scoped ViewModel scoped to this View. You will get the same instance every time for this Fragment, even
 * through rotation, or other configuration changes.
 *
 * This will walk up the view hierarchy until it finds a View that is the root view for a Fragment.
 *
 * If the ViewModel has additional dependencies, implement [MvRxViewModelFactory] in its companion object.
 * You will be given the initial state as well as a FragmentActivity with which you can access other dependencies to
 * pass to the ViewModel's constructor.
 *
 * MvRx will also handle persistence across process restarts. Refer to [PersistState] for more info.
 *
 * Use [keyFactory] if you have multiple ViewModels of the same class in the same scope.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.fragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : View, T : StatefulView {
    val viewLifecycleOwner = ViewLifecycleOwner(this)
    return lifecycleAwareLazy(viewLifecycleOwner, keyFactory) { key ->
        val fragment = requireNotNull(viewLifecycleOwner.fragment) { "View (${this@fragmentViewModel}) is not attached to a Fragment." }
        val activity = requireNotNull(viewLifecycleOwner.activity) { "View (${this@fragmentViewModel}) must be attached to an Activity." }

        val viewModel = MvRxViewModelProvider.get(viewModelClass.java, S::class.java, FragmentViewModelContext(activity, fragment._fragmentArgsProvider(), fragment, key), key)
        viewModel to viewModel.subscribe(viewLifecycleOwner, subscriber = { postOnInvalidate() })
    }
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different views or screens in the same activity.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.activityViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : View, T : StatefulView {
    val viewLifecycleOwner = ViewLifecycleOwner(this)
    return lifecycleAwareLazy(viewLifecycleOwner, keyFactory) { key ->
        val fragment = viewLifecycleOwner.fragment
        val activity = requireNotNull(viewLifecycleOwner.activity) { "View (${this@activityViewModel}) must be attached to an Activity." }
        require(activity is MvRxViewModelStoreOwner) { "Your Activity must be a MvRxViewModelStoreOwner!" }
        val args = if (fragment == null) activity._activityArgsProvider(null, key) else fragment._activityArgsProvider(key)

        val viewModel = MvRxViewModelProvider.get(viewModelClass.java, S::class.java, ActivityViewModelContext(activity, args, key), key)
        viewModel to viewModel.subscribe(viewLifecycleOwner, subscriber = { postOnInvalidate() })
    }
}

/**
 * For internal use only. Public for inline.
 *
 * Looks for [MvRx.KEY_ARG] on the arguments of the fragments.
 */
@Suppress("FunctionName")
@RestrictTo(LIBRARY)
fun <T : Fragment> T._fragmentArgsProvider(): Any? = arguments?.get(MvRx.KEY_ARG)

/**
 * For internal use only. Public for inline.
 *
 * Looks for [MvRx.KEY_ARG] on the arguments of the fragment receiver.
 *
 * Also adds the fragment's MvRx args to the host Activity's [MvRxViewModelStore] so that they can be used to recreate initial state
 * in a new process.
 */
@Suppress("FunctionName")
@RestrictTo(LIBRARY)
fun <T : Fragment> T._activityArgsProvider(key: String): Any? {
    val args: Any? = _fragmentArgsProvider()
    return requireActivity()._activityArgsProvider(args, key)
}

/**
 * For internal use only. Public for inline.
 *
 * Looks for [MvRx.KEY_ARG] on the arguments of the fragment receiver.
 *
 * Also adds the fragment's MvRx args to the host Activity's [MvRxViewModelStore] so that they can be used to recreate initial state
 * in a new process.
 */
@Suppress("FunctionName")
@RestrictTo(LIBRARY)
fun Activity._activityArgsProvider(args: Any?, key: String): Any? {
    if (this is MvRxViewModelStoreOwner) {
        mvrxViewModelStore._saveActivityViewModelArgs(key, args)
    } else {
        error("Your Activity must be a MvRxViewModelStoreOwner!")
    }
    return args
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.viewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : FragmentActivity, T : MvRxViewModelStoreOwner = lifecycleAwareLazy(this, keyFactory) { key ->
    val viewModel = MvRxViewModelProvider.get(viewModelClass.java, S::class.java, ActivityViewModelContext(this, intent.extras?.get(MvRx.KEY_ARG), key), key)
    viewModel to EmptyDisposable.INSTANCE
}

/**
 * Fragment argument delegate that makes it possible to set fragment args without
 * creating a key for each one.
 *
 * To create arguments, define a property in your fragment like:
 *     `private val listingId by arg<MyArgs>()`
 *
 * Each fragment can only have a single argument with the key [MvRx.KEY_ARG]
 */
fun <V : Any> args() = object : ReadOnlyProperty<Fragment, V> {
    var value: V? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): V {
        if (value == null) {
            val args = thisRef.arguments ?: throw IllegalArgumentException("There are no fragment arguments!")
            val argUntyped = args.get(MvRx.KEY_ARG)
            argUntyped ?: throw IllegalArgumentException("MvRx arguments not found at key MvRx.KEY_ARG!")
            @Suppress("UNCHECKED_CAST")
            value = argUntyped as V
        }
        return value ?: error("Fragment args was unexpectedly made null.")
    }
}

/**
 * Helper to handle pagination. Use this when you want to append a list of results at a given offset.
 * This is safer than just appending blindly to a list because it guarantees that the data gets added
 * at the offset it was requested at.
 *
 * This will replace *all contents* starting at the offset with the new list.
 * For example: [1,2,3].appendAt([4], 1) == [1,4]]
 */
fun <T : Any> List<T>.appendAt(other: List<T>?, offset: Int) = subList(0, offset.coerceIn(0, size)) + (other ?: emptyList())