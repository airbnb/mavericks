package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.ViewModelContext
import org.koin.androidx.scope.lifecycleScope
import org.koin.core.scope.Scope

typealias MvRxKoinScopeProvider = (ViewModelContext) -> Scope?

/**
 * Helper method for easy [MvRxKoinScopeProvider] creation.
 */
fun scopeProvider(block: MvRxKoinScopeProvider): MvRxKoinScopeProvider = block

/**
 * Default [MvRxKoinScopeProvider], which provides [org.koin.core.scope.Scope] tied to
 * specific Android class, depending on provided [ViewModelContext].
 */
val defaultScopeProvider: MvRxKoinScopeProvider = { context ->
    when (context) {
        is ActivityViewModelContext -> context.activity.lifecycleScope
        is FragmentViewModelContext -> context.fragment.lifecycleScope
    }
}