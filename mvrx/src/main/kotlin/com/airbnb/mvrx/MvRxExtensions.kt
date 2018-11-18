package com.airbnb.mvrx

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Gets or creates a ViewModel scoped to this Fragment. You will get the same instance every time for this Fragment, even
 * through rotation, or other configuration changes.
 *
 * Use [initializer] if you have something(like request form the repository) to do immediately when you get the ViewModel
 * Use [keyFactory] if you have multiple ViewModels of the same class in the same scope.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.fragmentViewModel(
        viewModelClass: KClass<VM> = VM::class,
        crossinline initializer: (VM) -> Unit = {},
        crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView, T : ViewModelFactoryOwner = lifecycleAwareLazy(this) {
    ViewModelProviders.of(this, viewModelFactory).get(keyFactory(), viewModelClass.java)
            .apply {
                initializer(this)
                subscribe(this@fragmentViewModel, subscriber = { postInvalidate() })
            }
}

/**
 * [fragmentViewModel] except scoped to its parent Fragment.
 * This can used to share state between different Fragments when they have the same parent Fragment(like ViewPager).
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.parentFragmentViewModel(
        viewModelClass: KClass<VM> = VM::class,
        crossinline initializer: (VM) -> Unit = {},
        crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView, T : ViewModelFactoryOwner = lifecycleAwareLazy(this) {
    ViewModelProviders.of(parentFragment!!, viewModelFactory).get(keyFactory(), viewModelClass.java)
            .apply {
                initializer(this)
                subscribe(this@parentFragmentViewModel, subscriber = { postInvalidate() })
            }
}

/**
 * [activityViewModel] except it will throw [IllegalStateException] if the ViewModel doesn't already exist.
 * Use this for screens in the middle of a flow that cannot reasonably be an entrypoint to the flow.
 * [existingInParentFragment] indicate where the existing ViewModel saved, `true` means the existing ViewModel
 * is saved in parent Fragment,`false` means it is saved in Activity.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, S : MvRxState> T.existingViewModel(
        viewModelClass: KClass<VM> = VM::class,
        crossinline initializer: (VM) -> Unit = {},
        existingInParentFragment: Boolean = false,
        crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = lifecycleAwareLazy(this) {
    if (existingInParentFragment) {
        ViewModelProviders.of(parentFragment!!, object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                throw IllegalStateException("ViewModel for $parentFragment[${keyFactory()}] does not exist yet!")
            }
        }).get(keyFactory(), viewModelClass.java)
                .apply {
                    initializer(this)
                    subscribe(this@existingViewModel, subscriber = { postInvalidate() })
                }
    } else {
        ViewModelProviders.of(requireActivity(), object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                throw IllegalStateException("ViewModel for ${requireActivity()}[${keyFactory()}] does not exist yet!")
            }
        }).get(keyFactory(), viewModelClass.java)
                .apply {
                    initializer(this)
                    subscribe(this@existingViewModel, subscriber = { postInvalidate() })
                }
    }
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.activityViewModel(
        viewModelClass: KClass<VM> = VM::class,
        crossinline initializer: (VM) -> Unit = {},
        crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView, T : ViewModelFactoryOwner = lifecycleAwareLazy(this) {
    ViewModelProviders.of(requireActivity(), viewModelFactory).get(keyFactory(), viewModelClass.java)
            .apply {
                initializer(this)
                subscribe(this@activityViewModel, subscriber = { postInvalidate() })
            }
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.viewModel(
        viewModelClass: KClass<VM> = VM::class,
        crossinline initializer: (VM) -> Unit = {},
        crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : FragmentActivity,
        T : ViewModelFactoryOwner = lifecycleAwareLazy(this) {
    ViewModelProviders.of(this, viewModelFactory).get(keyFactory(), viewModelClass.java)
            .apply {
                initializer(this)
            }
}

/**
 * Fragment argument delegate that makes it possible to set fragment args without
 * creating a key for each one.
 *
 * To create arguments, define a property in your fragment like:
 *     `private val listingId by args<MyArgs>()`
 *
 * Each fragment can only have a single argument with the key [MvRx.KEY_ARG]
 */
fun <V : Any> args() = object : ReadOnlyProperty<Fragment, V> {
    var value: V? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): V {
        if (value == null) {
            val args = thisRef.arguments
                    ?: throw IllegalArgumentException("There are no fragment arguments!")
            val argUntyped = args.get(MvRx.KEY_ARG)
            argUntyped
                    ?: throw IllegalArgumentException("MvRx arguments not found at key MvRx.KEY_ARG!")
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
 * For example: [1,2,3].appendAt([4], 2) == [1,2,4]]
 */
fun <T : Any> List<T>.appendAt(other: List<T>?, offset: Int) = subList(0, offset.coerceIn(0, size)) + (other ?: emptyList())
