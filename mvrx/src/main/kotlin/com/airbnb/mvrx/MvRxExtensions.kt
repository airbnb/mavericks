package com.airbnb.mvrx

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
inline fun <T, reified VM : BaseMavericksViewModel<S>, reified S : MvRxState> T.fragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): MavericksDelegateProvider<T, VM> where T : Fragment, T : MavericksView =
    viewModelDelegateProvider(
        viewModelClass,
        keyFactory,
        existingViewModel = false
    ) { stateFactory ->
        MvRxViewModelProvider.get(
            viewModelClass = viewModelClass.java,
            stateClass = S::class.java,
            viewModelContext = FragmentViewModelContext(
                activity = requireActivity(),
                args = _fragmentArgsProvider(),
                fragment = this
            ),
            key = keyFactory(),
            initialStateFactory = stateFactory
        )
    }

/**
 * Gets or creates a ViewModel scoped to a parent fragment. This delegate will walk up the parentFragment hierarchy
 * until it finds a Fragment that can provide the correct ViewModel. If no parent fragments can provide the ViewModel,
 * a new one will be created in top-most parent Fragment.
 */
inline fun <T, reified VM : BaseMavericksViewModel<S>, reified S : MvRxState> T.parentFragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): MavericksDelegateProvider<T, VM> where T : Fragment, T : MavericksView =
    viewModelDelegateProvider(
        viewModelClass,
        keyFactory,
        existingViewModel = true
    ) { stateFactory ->
        // 'existingViewModel' is set to true. Although this function works in both cases of
        // either existing or new viewmodel it would be more difficult to support both cases,
        // so we just test the common case of "existing". We can't be sure that the fragment
        // was designed for it to be used in the non-existing case (ie it may require arguments)

        requireNotNull(parentFragment) { "There is no parent fragment for ${this::class.java.simpleName}!" }
        var parent: Fragment? = parentFragment
        val key = keyFactory()
        while (parent != null) {
            try {
                return@viewModelDelegateProvider MvRxViewModelProvider.get(
                    viewModelClass = viewModelClass.java,
                    stateClass = S::class.java,
                    viewModelContext = FragmentViewModelContext(
                        activity = this.requireActivity(),
                        args = _fragmentArgsProvider(),
                        fragment = parent
                    ),
                    key = key,
                    forExistingViewModel = true
                )
            } catch (e: ViewModelDoesNotExistException) {
                parent = parent.parentFragment
            }
        }

        // ViewModel was not found. Create a new one in the top-most parent.
        var topParentFragment = parentFragment
        while (topParentFragment?.parentFragment != null) {
            topParentFragment = topParentFragment.parentFragment
        }
        val viewModelContext = FragmentViewModelContext(
            requireActivity(),
            _fragmentArgsProvider(),
            topParentFragment!!
        )

        MvRxViewModelProvider.get(
            viewModelClass = viewModelClass.java,
            stateClass = S::class.java,
            viewModelContext = viewModelContext,
            key = keyFactory(),
            initialStateFactory = stateFactory
        )
    }

/**
 * Gets or creates a ViewModel scoped to a target fragment. Throws [IllegalStateException] if there is no target fragment.
 */
inline fun <T, reified VM : BaseMavericksViewModel<S>, reified S : MvRxState> T.targetFragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): MavericksDelegateProvider<T, VM> where T : Fragment, T : MavericksView =
    viewModelDelegateProvider(
        viewModelClass,
        keyFactory,
        existingViewModel = true
    ) { stateFactory ->
        // 'existingViewModel' is set to true. Although this function works in both cases of
        // either existing or new viewmodel it would be more difficult to support both cases,
        // so we just test the common case of "existing". We can't be sure that the fragment
        // was designed for it to be used in the non-existing case (ie it may require arguments)

        val targetFragment =
            requireNotNull(targetFragment) { "There is no target fragment for ${this::class.java.simpleName}!" }

        MvRxViewModelProvider.get(
            viewModelClass = viewModelClass.java,
            stateClass = S::class.java,
            viewModelContext = FragmentViewModelContext(
                activity = requireActivity(),
                args = targetFragment._fragmentArgsProvider(),
                fragment = targetFragment
            ),
            key = keyFactory(),
            initialStateFactory = stateFactory
        )
    }

/**
 * [activityViewModel] except it will throw [IllegalStateException] if the ViewModel doesn't already exist.
 * Use this for screens in the middle of a flow that cannot reasonably be an entrypoint to the flow.
 */
inline fun <T, reified VM : BaseMavericksViewModel<S>, reified S : MvRxState> T.existingViewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): MavericksDelegateProvider<T, VM> where T : Fragment, T : MavericksView =
    viewModelDelegateProvider(
        viewModelClass,
        keyFactory,
        existingViewModel = true
    ) { stateFactory ->

        MvRxViewModelProvider.get(
            viewModelClass = viewModelClass.java,
            stateClass = S::class.java,
            viewModelContext = ActivityViewModelContext(
                requireActivity(),
                _fragmentArgsProvider()
            ),
            key = keyFactory(),
            initialStateFactory = stateFactory,
            forExistingViewModel = true
        )
    }

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMavericksViewModel<S>, reified S : MvRxState> T.activityViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
): MavericksDelegateProvider<T, VM> where T : Fragment, T : MavericksView =
    viewModelDelegateProvider(
        viewModelClass,
        keyFactory,
        existingViewModel = false
    ) { stateFactory ->

        MvRxViewModelProvider.get(
            viewModelClass = viewModelClass.java,
            stateClass = S::class.java,
            viewModelContext = ActivityViewModelContext(
                activity = requireActivity(),
                args = _fragmentArgsProvider()
            ),
            key = keyFactory(),
            initialStateFactory = stateFactory
        )
    }

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, reified VM : BaseMavericksViewModel<S>, reified S : MvRxState> T.viewModel(
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : FragmentActivity = lifecycleAwareLazy(this) {
    MvRxViewModelProvider.get(
        viewModelClass = viewModelClass.java,
        stateClass = S::class.java,
        viewModelContext = ActivityViewModelContext(this, intent.extras?.get(MvRx.KEY_ARG)),
        key = keyFactory()
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
 * For example: [1,2,3].appendAt([4], 1) == [1,4]]
 */
fun <T : Any> List<T>.appendAt(other: List<T>?, offset: Int) =
    subList(0, offset.coerceIn(0, size)) + (other ?: emptyList())