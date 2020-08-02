package com.airbnb.mvrx.hellokoin.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.existingViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.targetFragmentViewModel
import com.airbnb.mvrx.viewModel
import kotlin.reflect.KClass


inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.koinFragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline scopeProvider: MvRxKoinScopeProvider = defaultScopeProvider,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = fragmentViewModel(viewModelClass, scopeProvider, keyFactory)

inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.koinParentFragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline scopeProvider: MvRxKoinScopeProvider = defaultScopeProvider,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : Fragment, T : MvRxView = parentFragmentViewModel(viewModelClass, scopeProvider, keyFactory)

inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.koinTargetFragmentViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline scopeProvider: MvRxKoinScopeProvider = defaultScopeProvider,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : Fragment, T : MvRxView = targetFragmentViewModel(viewModelClass, scopeProvider, keyFactory)

inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.koinExistingViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline scopeProvider: MvRxKoinScopeProvider = defaultScopeProvider,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = existingViewModel(viewModelClass, scopeProvider, keyFactory)

inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.koinActivityViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline scopeProvider: MvRxKoinScopeProvider = defaultScopeProvider,
    noinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : Fragment, T : MvRxView = activityViewModel(viewModelClass, scopeProvider, keyFactory)

inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.koinViewModel(
    viewModelClass: KClass<VM> = VM::class,
    noinline scopeProvider: MvRxKoinScopeProvider = defaultScopeProvider,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
) where T : FragmentActivity = viewModel(viewModelClass, scopeProvider, keyFactory)