package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this) {
    MvRxViewModelProvider.get(
        viewModelClass.java,
        S::class.java,
        FragmentViewModelContext(this.requireActivity(), _fragmentArgsProvider(), this),
        keyFactory()
    ).apply { subscribe(this@fragmentViewModel, subscriber = { postInvalidate() }) }
}

/**
 * Gets or creates a ViewModel scoped to a parent fragment. This delegate will walk up the parentFragment hierarchy
 * until it finds a Fragment that can provide the correct ViewModel. If no parent fragments can provide the ViewModel,
 * a new one will be created in top-most parent Fragment.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.parentFragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : Fragment, T : MvRxView = lifecycleAwareLazy(this) {
    requireNotNull(parentFragment) { "There is no parent fragment for ${this::class.java.simpleName}!" }
    var fragment: Fragment? = parentFragment
    val key = keyFactory()
    while (fragment != null) {
        try {
            return@lifecycleAwareLazy MvRxViewModelProvider.get(
                viewModelClass.java,
                S::class.java,
                FragmentViewModelContext(this.requireActivity(), _fragmentArgsProvider(), fragment),
                key,
                forExistingViewModel = true
            ).apply { subscribe(this@parentFragmentViewModel, subscriber = { postInvalidate() }) }
        } catch (e: ViewModelDoesNotExistException) {
            fragment = fragment.parentFragment
        }
    }

    // ViewModel was not found. Create a new one in the top-most parent.
    var topParentFragment = parentFragment
    while (topParentFragment?.parentFragment != null) {
        topParentFragment = topParentFragment.parentFragment
    }
    val viewModelContext = FragmentViewModelContext(this.requireActivity(), _fragmentArgsProvider(), topParentFragment!!)
    return@lifecycleAwareLazy MvRxViewModelProvider.get(viewModelClass.java, S::class.java, viewModelContext, keyFactory())
        .apply { subscribe(this@parentFragmentViewModel, subscriber = { postInvalidate() }) }
}

/**
 * Gets or creates a ViewModel scoped to a target fragment. Throws [IllegalStateException] if there is no target fragment.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.targetFragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : Fragment, T : MvRxView = lifecycleAwareLazy(this) {
    val targetFragment = requireNotNull(targetFragment) { "There is no target fragment for ${this::class.java.simpleName}!" }
    MvRxViewModelProvider.get(
        viewModelClass.java,
        S::class.java,
        FragmentViewModelContext(this.requireActivity(), targetFragment._fragmentArgsProvider(), targetFragment),
        keyFactory()
    )
        .apply { subscribe(this@targetFragmentViewModel, subscriber = { postInvalidate() }) }
}

/**
 * [activityViewModel] except it will throw [IllegalStateException] if the ViewModel doesn't already exist.
 * Use this for screens in the middle of a flow that cannot reasonably be an entrypoint to the flow.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.existingViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this) {
    MvRxViewModelProvider.get(
        viewModelClass.java,
        S::class.java,
        ActivityViewModelContext(requireActivity(), _fragmentArgsProvider()),
        keyFactory(),
        forExistingViewModel = true
    ).apply { subscribe(this@existingViewModel, subscriber = { postInvalidate() }) }
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.activityViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this) {
    MvRxViewModelProvider.get(
        viewModelClass.java,
        S::class.java,
        ActivityViewModelContext(requireActivity(), _fragmentArgsProvider()),
        keyFactory()
    ).apply { subscribe(this@activityViewModel, subscriber = { postInvalidate() }) }
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
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.viewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : FragmentActivity = lifecycleAwareLazy(this) {
    MvRxViewModelProvider.get(
        viewModelClass.java,
        S::class.java,
        ActivityViewModelContext(this, intent.extras?.get(MvRx.KEY_ARG)),
        keyFactory()
    )
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
        return value ?: throw IllegalArgumentException("")
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
