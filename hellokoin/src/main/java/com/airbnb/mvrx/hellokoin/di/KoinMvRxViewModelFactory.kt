package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import org.koin.core.context.KoinContextHandler
import org.koin.core.parameter.parametersOf


abstract class KoinMvRxViewModelFactory<VM : BaseViewModel<S>, S : MvRxState>(
        private val viewModelClass: Class<out BaseViewModel<S>>
) : MvRxViewModelFactory<VM, S> {

    @Suppress("UNCHECKED_CAST")
    override fun create(viewModelContext: ViewModelContext, state: S): VM? {
        val koinScope = (viewModelContext.customData as? MvRxKoinScopeProvider)
                ?.invoke(viewModelContext)
                ?: KoinContextHandler.get()._scopeRegistry.rootScope

        return koinScope.get(clazz = viewModelClass) {
            parametersOf(state)
        }
    }
}