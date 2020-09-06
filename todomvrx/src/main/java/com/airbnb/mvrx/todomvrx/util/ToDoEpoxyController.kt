package com.airbnb.mvrx.todomvrx.util

import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.core.MvRxViewModel
import com.airbnb.mvrx.withState

class ToDoEpoxyController(
    val buildModelsCallback: EpoxyController.() -> Unit = {}
) : AsyncEpoxyController() {
    override fun buildModels() {
        buildModelsCallback()
    }
}

/**
 * Create a [MvRxEpoxyController] that builds models with the given callback.
 */
fun BaseFragment.simpleController(
    buildModels: EpoxyController.() -> Unit
) = ToDoEpoxyController {
    // Models are built asynchronously, so it is possible that this is called after the fragment
    // is detached under certain race conditions.
    if (view == null || isRemoving) return@ToDoEpoxyController
    buildModels()
}

/**
 * Create a [ToDoEpoxyController] that builds models with the given callback.
 * When models are built the current state of the viewmodel will be provided.
 */
fun <S : MavericksState, A : MvRxViewModel<S>> BaseFragment.simpleController(
    viewModel: A,
    buildModels: EpoxyController.(state: S) -> Unit
) = ToDoEpoxyController {
    if (view == null || isRemoving) return@ToDoEpoxyController
    com.airbnb.mvrx.withState(viewModel) { state ->
        buildModels(state)
    }
}

fun <A : BaseMvRxViewModel<B>, B : MavericksState, C : BaseMvRxViewModel<D>, D : MavericksState> BaseFragment.simpleController(
    viewModel1: A,
    viewModel2: C,
    buildModels: EpoxyController.(state1: B, state2: D) -> Unit
) = ToDoEpoxyController {
    if (view == null || isRemoving) return@ToDoEpoxyController
    withState(viewModel1, viewModel2) { state1, state2 ->
        buildModels(state1, state2)
    }
}
