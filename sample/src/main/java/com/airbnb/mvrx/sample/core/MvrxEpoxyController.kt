package com.airbnb.mvrx.sample.core

import android.os.Bundle
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.withState


/**
 * For use with [BaseFragment.epoxyController].
 *
 * This builds Epoxy models in a background thread.
 */
open class MvRxEpoxyController(
    val buildModelsCallback: EpoxyController.() -> Unit = {}
) : AsyncEpoxyController() {

    override fun buildModels() {
        buildModelsCallback()
    }

    override fun onRestoreInstanceState(inState: Bundle?) {
        // In Epoxy, you normally own your adapter and save it as a field so you can safely call onSave and onRestoreInstanceState.
        // MvRx provides an epoxy controller by default but allows it to be manually set. In onSaveInstanceState, we query the RecyclerView
        // for its adapter and save its state. This works unless onDestroyView was called first in which case the adapter gets cleared.
        // This happens if onSaveInstanceState is called while a Fragment is on the back stack.
        // In this case, when return, savedInstanceState will be called with a non-null bundle but the epoxy state was never saved.
        // Epoxy crashes in this case because it's normally a sign that you forgot to save the state. However, in this case, it happens because
        // we don't own the EpoxyController.
        // This does the hacky thing of checking for the epoxy key because Epoxy has no way of handling this since. TBD if it is something
        // it should support.
        if (inState?.containsKey("saved_state_view_holders") == true) {
            super.onRestoreInstanceState(inState)
        }
    }
}


/**
 * Create a [MvRxEpoxyController] that builds models with the given callback.
 */
fun BaseFragment.simpleController(
    buildModels: EpoxyController.() -> Unit
) = MvRxEpoxyController {
    // Models are built asynchronously, so it is possible that this is called after the fragment
    // is detached under certain race conditions.
    if (view == null || isRemoving) return@MvRxEpoxyController
    buildModels()
}

/**
 * Create a [MvRxEpoxyController] that builds models with the given callback.
 * When models are built the current state of the viewmodel will be provided.
 */
fun <S : MvRxState, A : MvRxViewModel<S>> BaseFragment.simpleController(
    viewModel: A,
    buildModels: EpoxyController.(state: S) -> Unit
) = MvRxEpoxyController {
    if (view == null || isRemoving) return@MvRxEpoxyController
    withState(viewModel) { state ->
        buildModels(state)
    }
}

/**
 * Create a [MvRxEpoxyController] that builds models with the given callback.
 * When models are built the current state of the viewmodels will be provided.
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState> BaseFragment.simpleController(
    viewModel1: A,
    viewModel2: C,
    buildModels: EpoxyController.(state1: B, state2: D) -> Unit
) = MvRxEpoxyController {
    if (view == null || isRemoving) return@MvRxEpoxyController
    withState(viewModel1, viewModel2) { state1, state2 ->
        buildModels(state1, state2)
    }
}

/**
 * Create a [MvRxEpoxyController] that builds models with the given callback.
 * When models are built the current state of the viewmodels will be provided.
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState, E : BaseMvRxViewModel<F>, F : MvRxState> BaseFragment.simpleController(
    viewModel1: A,
    viewModel2: C,
    viewModel3: E,
    buildModels: EpoxyController.(state1: B, state2: D, state3: F) -> Unit
) = MvRxEpoxyController {
    if (view == null || isRemoving) return@MvRxEpoxyController
    withState(viewModel1, viewModel2, viewModel3) { state1, state2, state3 ->
        buildModels(state1, state2, state3)
    }
}