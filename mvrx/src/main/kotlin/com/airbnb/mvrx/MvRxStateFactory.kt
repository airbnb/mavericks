package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import java.lang.reflect.Modifier

@RestrictTo(RestrictTo.Scope.LIBRARY)
interface MvRxStateFactory<VM : BaseMvRxViewModel<S>, S : MvRxState> {

    fun createInitialState(
        viewModelClass: Class<out VM>,
        stateClass: Class<out S>,
        viewModelContext: ViewModelContext,
        stateRestorer: (S) -> S
    ): S
}

internal class RealMvRxStateFactory<VM : BaseMvRxViewModel<S>, S : MvRxState> : MvRxStateFactory<VM, S> {

    override fun createInitialState(
        viewModelClass: Class<out VM>,
        stateClass: Class<out S>,
        viewModelContext: ViewModelContext,
        stateRestorer: (S) -> S
    ): S {
        val factoryState = createStateFromCompanionFactory(viewModelClass, viewModelContext)
        return stateRestorer(
            factoryState
                ?: createStateFromConstructor(viewModelClass, stateClass, viewModelContext.args)
        )
    }
}

/**
 * Searches the companion of [viewModelClass] for an initialState function, and uses it to create the initial state.
 * If no such function exists, null is returned.
 */
@Suppress("UNCHECKED_CAST")
internal fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createStateFromCompanionFactory(
    viewModelClass: Class<out VM>,
    viewModelContext: ViewModelContext
): S? {
    return viewModelClass.factoryCompanion()?.let { factoryClass ->
        try {
            factoryClass.getMethod("initialState", ViewModelContext::class.java)
                .invoke(factoryClass.instance(), viewModelContext) as S?
        } catch (exception: NoSuchMethodException) {
            // Check for JvmStatic method.
            viewModelClass.getMethod("initialState", ViewModelContext::class.java)
                .invoke(null, viewModelContext) as S?
        }
    }
}

/**
 * Searches [stateClass] for a single argument constructor matching the type of [args]. If [args] is null, then
 * no arg constructor is invoked.
 */
internal fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createStateFromConstructor(
    viewModelClass: Class<out VM>,
    stateClass: Class<out S>,
    args: Any?
): S {
    val argsConstructor = args?.let { arg ->
        val argType = arg::class.java
        stateClass.constructors.firstOrNull { constructor ->
            constructor.parameterTypes.size == 1 && isAssignableTo(argType, constructor.parameterTypes[0])
        }
    }

    @Suppress("UNCHECKED_CAST")
    return argsConstructor?.newInstance(args) as? S
        ?: try {
            if (Modifier.isPublic(stateClass.modifiers)) {
                stateClass.newInstance()
            } else {
                stateClass.constructors.firstOrNull { it.parameterCount == 0 }?.let { c ->
                    c.isAccessible = true
                    c.newInstance() as? S
                }
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            // Throw this exception if something we explicitly tried failed.
            throw java.lang.IllegalStateException("Failed to create initial state!", e)
        }
        // Throw this exception if we don't know which method to use to create the initial state.
        ?: throw IllegalStateException(
            "Attempt to create the MvRx state class ${stateClass.simpleName} has failed. One of the following must be true:" +
                "\n 1) The state class has default values for every constructor property." +
                "\n 2) The state class has a secondary constructor for ${args?.javaClass?.simpleName ?: "a fragment argument"}." +
                "\n 3) ${viewModelClass.simpleName} must have a companion object implementing MvRxFactory with an initialState function " +
                "that does not return null. "
        )
}
