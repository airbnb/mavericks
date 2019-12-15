package com.airbnb.mvrx.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.*
import kotlin.reflect.KClass

/**
 * Gets or creates a ViewModel scoped to a navigation graph ID.
 * [IllegalArgumentException] if the navGraphId destination is not on the back stack.
 */
inline fun <reified T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> T.navGraphViewModel(
    @IdRes navGraphId: Int,
    viewModelClass: KClass<VM> = VM::class,
    crossinline keyFactory: () -> String = { viewModelClass.java.name }
): Lazy<VM> where T : Fragment, T : MvRxView {
    val backStackEntry by lazy {
        findNavController().getBackStackEntry(navGraphId)
    }
    val storeProducer: () -> ViewModelStore = {
        backStackEntry.viewModelStore
    }

    val viewModelLazy by lazy {
        MvRxViewModelProvider.get(
            viewModelClass.java,
            S::class.java,
            FragmentViewModelContext(
                requireActivity(),
                backStackEntry.arguments?.get(MvRx.KEY_ARG),
                this,
                backStackEntry,
                backStackEntry.savedStateRegistry
            ),
            keyFactory()
        ).apply { subscribe(this@navGraphViewModel, subscriber = { postInvalidate() }) }
    }

    return createViewModelLazy(VM::class, storeProducer, {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                viewModelLazy as T
        }
    })
}
