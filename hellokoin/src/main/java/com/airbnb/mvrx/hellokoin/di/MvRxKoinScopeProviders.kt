package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.ViewModelContext
import org.koin.androidx.scope.lifecycleScope
import org.koin.core.scope.Scope

typealias MvRxKoinScopeProvider = (ViewModelContext) -> Scope?

fun scopeProvider(block: MvRxKoinScopeProvider): MvRxKoinScopeProvider = block

val defaultScopeProvider: MvRxKoinScopeProvider = { context ->
    when (context) {
        is ActivityViewModelContext -> context.activity.lifecycleScope
        is FragmentViewModelContext -> context.fragment.lifecycleScope
    }
}