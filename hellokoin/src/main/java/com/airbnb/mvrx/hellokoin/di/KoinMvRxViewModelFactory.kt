package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import org.koin.core.context.KoinContextHandler
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.error.NoScopeDefFoundException
import org.koin.core.parameter.parametersOf


/**
 * A [MvRxViewModelFactory] which makes it easy to use existing [Koin] container with [viewModel]
 * factory methods for ViewModel creation. This class should be implemented by the companion object
 * of every ViewModel which wants to use [Koin] bindings.
 *
 * This class accesses either [org.koin.core.scope.Scope] provided by [MvRxKoinScopeProvider] from
 * [ViewModelContext], or rootScope of [Koin] (note, that prior to this, `startKoin` should be called).
 * Than the scope is used to get or create ViewModel for requested [viewModelClass].
 *
 * Example:
 *
 * class MyViewModel constructor(...): BaseViewModel<MyState>(...) {
 *
 *   companion object : KoinMvRxViewModelFactory<MyViewModel, MyState>(MyViewModel::class.java)
 *
 * }
 *
 * @param viewModelClass The [Class] of the ViewModel being requested for creation
 */
abstract class KoinMvRxViewModelFactory<VM : BaseMvRxViewModel<S>, S : MvRxState>(
    private val viewModelClass: Class<out BaseMvRxViewModel<S>>
) : MvRxViewModelFactory<VM, S> {

    @Suppress("UNCHECKED_CAST")
    override fun create(viewModelContext: ViewModelContext, state: S): VM? {
        val koinScope = try {
            (viewModelContext.customData as? MvRxKoinScopeProvider)
                ?.invoke(viewModelContext)
                ?: KoinContextHandler.get()._scopeRegistry.rootScope
        } catch (e: IllegalStateException) {
            /**
             * If couldn't get [rootScope],
             * throw [KoinNoScopeFoundException] to show problem with setup.
             */
            throw KoinNoScopeFoundException(e)
        } catch (e: NoScopeDefFoundException) {
            /**
             * If couldn't get or create [org.koin.core.scope.Scope] from [ViewModelContext],
             * throw [KoinNoScopeFoundException] to show problem with setup.
             */
            throw KoinNoScopeFoundException(e)
        }

        return try {
            koinScope.get(clazz = viewModelClass) {
                parametersOf(state)
            }
        } catch (e: NoBeanDefFoundException) {
            /**
             * If no factory method was found in [koinScope] for given [viewModelClass],
             * throw [KoinNoFactoryFoundException] to show problem with setup.
             */
            throw KoinNoFactoryFoundException(e)
        }
    }
}

class KoinNoScopeFoundException(cause: Throwable? = null) : Exception(cause)
class KoinNoFactoryFoundException(cause: Throwable? = null) : Exception(cause)