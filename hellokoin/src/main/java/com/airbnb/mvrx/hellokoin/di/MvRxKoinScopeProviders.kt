package com.airbnb.mvrx.hellokoin.di

import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.ViewModelContext
import org.koin.androidx.scope.lifecycleScope
import org.koin.core.error.NoScopeDefFoundException
import org.koin.core.scope.Scope

typealias MvRxKoinScopeProvider = (ViewModelContext) -> Scope?

val defaultScopeProvider: MvRxKoinScopeProvider = { context ->
    when (context) {
        is ActivityViewModelContext -> context.activity.safeLifecycleScope
        is FragmentViewModelContext -> context.fragment.safeLifecycleScope
                ?: context.activity.safeLifecycleScope
    }
}

private val LifecycleOwner.safeLifecycleScope: Scope?
    get() = try {
        lifecycleScope
    } catch (e: NoScopeDefFoundException) {
        null
    }
