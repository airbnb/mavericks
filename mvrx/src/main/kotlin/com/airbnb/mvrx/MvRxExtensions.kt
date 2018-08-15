package com.airbnb.mvrx

import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

/**
 * Gets or creates a ViewModel scoped to this Fragment. You will get the same instance every time for this Fragment, even
 * through rotation, or other configuration changes.
 *
 * If the ViewModel does not yet exist, the stateFactory will be used to create the initial state.
 *
 * If your ViewModel has no additional dependencies, just make sure that it has a single constructor parameter for initialState.
 *
 * If it it has additional dependencies, override [com.airbnb.mvrx.MvRxViewModelFactory] in its companion object. You will be given this state
 * as well as a FragmentActivity with which you can use the getOrCreateSubcomponent extension method on FragmentActivity to
 * access your Dagger subcomponent where the rest of your dependencies live.
 *
 * MvRx will also handle persistence across process restarts. Refer to [com.airbnb.mvrx.PersistState] for more info.
 *
 * Use shouldUpdate if you only want to subscribe to a subset of all updates. There are some standard ones in ShouldUpdateHelpers.
 *
 * Use keyFactory if you have multiple ViewModels of the same class in the same scope.
 */
inline fun <T, VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.fragmentViewModel(
    viewModelClass: KClass<VM>,
    noinline shouldUpdate: ((S, S) -> Boolean)? = null,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment,
        T : MvRxView = lazy {
    val stateFactory: () -> S = ::_fragmentViewModelInitialStateProvider
    MvRxViewModelProvider.get(viewModelClass, this, keyFactory(), stateFactory)
        .apply { subscribe(requireActivity(), shouldUpdate = shouldUpdate, subscriber = { invalidate() }) }
}

/**
 * [activityViewModel] except it will throw [IllegalStateException] if the ViewModel doesn't already exist.
 * Use this for screens in the middle of a flow that cannot reasonably be an entrypoint to the flow.
 */
fun <T, VM : BaseMvRxViewModel<S>, S : MvRxState> T.existingViewModel(
    viewModelClass: KClass<VM>,
    shouldUpdate: ((S, S) -> Boolean)? = null,
    keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment,
        T : MvRxView = lazy {
    val factory = MvRxFactory { throw IllegalStateException("ViewModel for ${requireActivity()}[${keyFactory()}] does not exist yet!") }
    ViewModelProviders.of(requireActivity(), factory).get(keyFactory(), viewModelClass.java)
        .apply { subscribe(requireActivity(), shouldUpdate = shouldUpdate, subscriber = { invalidate() }) }
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 *
 * @param stateFactory A lambda that provides an instance of your State class; this will be used to initialize the ViewModel. If no lambda is provided,
 * the default will attempt to instantiate your state class with the arguments provided to the fragment.
 * If the fragment has no arguments set then state will be initialized with a zero argument constructor. If neither exist an exception will be thrown.
 */
inline fun <T, VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.activityViewModel(
    viewModelClass: KClass<VM>,
    noinline shouldUpdate: ((S, S) -> Boolean)? = null,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment,
        T : MvRxView = lazy {
    val stateFactory: () -> S = { _activityViewModelInitialStateProvider(keyFactory) }
    if (requireActivity() !is MvRxViewModelStoreOwner) throw IllegalArgumentException("Your Activity must be a MvRxViewModelStoreOwner!")
    MvRxViewModelProvider.get(viewModelClass, requireActivity(), keyFactory(), stateFactory)
        .apply { subscribe(this@activityViewModel, shouldUpdate = shouldUpdate, subscriber = { invalidate() }) }
}

/**
 * For internal use only. Public for inline.
 *
 * Looks for [MvRx.KEY_ARG] on the arguments of the fragment receiver to create an instance of the State class.
 * The state class must have a matching single arg constructor.
 *
 * If no MvRx fragment args exist it attempts to use a empty constructor. Otherwise an exception is thrown.
 *
 * Also adds the fragment's MvRx args to the host Activity's [MvRxViewModelStore] so that they can be used to recreate initial state
 * in a new process.
 */
@Suppress("FunctionName")
inline fun <reified S : MvRxState, T : Fragment> T._activityViewModelInitialStateProvider(keyFactory: () -> String): S {
    val args: Any? = arguments?.get(MvRx.KEY_ARG)
    val activity = requireActivity()
    if (activity is MvRxViewModelStoreOwner) {
        activity.mvrxViewModelStore._saveActivityViewModelArgs(keyFactory(), args)
    } else {
        throw IllegalArgumentException("Your Activity must be a MvRxViewModelStoreOwner!")
    }
    return _initialStateProvider(S::class, args)
}

/**
 * For internal use only. Public for inline.
 *
 * Looks for [MvRx.KEY_ARG] on the arguments of the fragment receiver to create an instance of the State class.
 * The state class must have a matching single arg constructor.
 *
 * If no MvRx fragment args exist it attempts to use a empty constructor. Otherwise an exception is thrown.
 */
@Suppress("FunctionName")
inline fun <reified S : MvRxState, T : Fragment> T._fragmentViewModelInitialStateProvider(): S {
    val args: Any? = arguments?.get(MvRx.KEY_ARG)
    return _initialStateProvider(S::class, args)
}

/**
 * For internal use only. Public for inline.
 *
 * Looks for [MvRx.KEY_ARG] in intent extras on activity receiver to create an instance of the State class.
 * The state class must have a matching single arg constructor.
 *
 * If no MvRx activity args exist it attempts to use a empty constructor. Otherwise an exception is thrown.
 */
@Suppress("FunctionName")
inline fun <reified S : MvRxState, T : FragmentActivity> T._activityViewModelInitialStateProvider(): S {
    val args: Any? = intent.extras?.get(MvRx.KEY_ARG)
    return _initialStateProvider(S::class, args)
}

/**
 * For internal use only. Public for inline.
 *
 * Searches [stateClass] for a single argument constructor matching the type of [args]. If [args] is null, then
 * no arg constructor is invoked.
 *
 */
@Suppress("FunctionName") // Public for inline.
fun <S : MvRxState> _initialStateProvider(stateClass: KClass<S>, args: Any?): S {
    val argsConstructor = args?.let {
        val argType = it::class.createType()
        stateClass.constructors
            .firstOrNull {
                it.parameters
                    .singleOrNull()
                    ?.type
                    ?.isSupertypeOf(argType)
                    ?: false
            }
    }

    // TODO eli_hart: 5/29/18 If/when arguments are hidden from public usage, and can only be used to create state, we can fail if args exist
    // with no matching constructor. For now we support the legacy case of fragment args existing but
    // the empty constructor still being used (when the dev accesses args directly).
    return argsConstructor?.call(args)
        ?: try {
            stateClass.java.newInstance()
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            null
        }
        ?: throw IllegalStateException(
            "Attempt to auto create the mvrx state class ${stateClass.simpleName} has failed. It must have default values for every property or a " +
                "secondary constructor for ${args?.javaClass?.simpleName ?: "a fragment argument"}. "
        )
}

/**
 * [fragmentViewModel] except scoped to the current Activity. Use this to share state between different Fragments.
 */
inline fun <T, VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.viewModel(
    viewModelClass: KClass<VM>,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : FragmentActivity,
        T : MvRxViewModelStoreOwner = lazy {
    val stateFactory: () -> S = { _activityViewModelInitialStateProvider() }
    MvRxViewModelProvider.get(viewModelClass, this, keyFactory(), stateFactory)
}

/**
 * Fragment argument delegate that makes it possible to set fragment args without
 * creating a key for each one.
 *
 * To create arguments, define a propety in your fragment like:
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
 * For example: [1,2,3].appendAt([4], 2) == [1,4]]
 */
fun <T : Any> List<T>.appendAt(other: List<T>?, offset: Int) = subList(0, offset.coerceIn(0, size)) + (other ?: emptyList())