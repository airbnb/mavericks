package com.airbnb.mvrx.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
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
): navigationLifecycleAwareLazy<VM> where T : Fragment, T : MvRxView = navigationLifecycleAwareLazy(subscriptionLifecycleOwner) {
    val backStackEntry = findNavController().getBackStackEntry(navGraphId)

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
