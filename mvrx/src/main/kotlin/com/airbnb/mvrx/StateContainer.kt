package com.airbnb.mvrx


/**
 * Access ViewModelState from a single ViewModel asynchronously on merge thread
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C> withAsyncState(viewModel1: A, block: (B) -> C) = viewModel1.withState { block(it) }

/**
 * Accesses ViewModel state from a single ViewModel synchronously on main thread, and returns the result of the block.
 * This is only for UI rendering purpose to render the current state
 * For non-UI logic please use withAsyncState to avoid race condition
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C> withRenderingState(viewModel1: A, block: (B) -> C) = block(viewModel1.renderingState)


/**
 * Access ViewModelState from two ViewModels asynchronously on merge thread
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState, E> withAsyncState(
        viewModel1: A,
        viewModel2: C,
        block: (B, D) -> E
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> block(s1, s2) } }

/**
 * Accesses ViewModel state from two ViewModels synchronously on main thread, and returns the result of the block.
 * This is only for UI rendering purpose to render the current state
 * For non-UI logic please use withAsyncState to avoid race condition
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState, E> withRenderingState(
        viewModel1: A,
        viewModel2: C,
        block: (B, D) -> E
) = block(viewModel1.renderingState, viewModel2.renderingState)


/**
 * Access ViewModelState from three ViewModels asynchronously on merge thread
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState, E : BaseMvRxViewModel<F>, F : MvRxState, G> withAsyncState(
        viewModel1: A,
        viewModel2: C,
        viewModel3: E,
        block: (B, D, F) -> G
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> viewModel3.withState { s3 -> block(s1, s2, s3) } } }

/**
 * Accesses ViewModel state from three ViewModels synchronously on main thread, and returns the result of the block.
 * This is only for UI rendering purpose to render the current state
 * For non-UI logic please use withAsyncState to avoid race condition
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState, E : BaseMvRxViewModel<F>, F : MvRxState, G> withRenderingState(
        viewModel1: A,
        viewModel2: C,
        viewModel3: E,
        block: (B, D, F) -> G
) = block(viewModel1.renderingState, viewModel2.renderingState, viewModel3.renderingState)


/**
 * Access ViewModelState from four ViewModels asynchronously on merge thread
 */
fun <
        A : BaseMvRxViewModel<B>, B : MvRxState,
        C : BaseMvRxViewModel<D>, D : MvRxState,
        E : BaseMvRxViewModel<F>, F : MvRxState,
        G : BaseMvRxViewModel<H>, H : MvRxState,
        I
        > withAsyncState(
        viewModel1: A,
        viewModel2: C,
        viewModel3: E,
        viewModel4: G,
        block: (B, D, F, H) -> I
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> viewModel3.withState { s3 -> viewModel4.withState { s4 -> block(s1, s2, s3, s4) } } } }

/**
 * Accesses ViewModel state from four ViewModels synchronously on main thread, and returns the result of the block.
 * This is only for UI rendering purpose to render the current state
 * For non-UI logic please use withAsyncState to avoid race condition
 */
fun <
        A : BaseMvRxViewModel<B>, B : MvRxState,
        C : BaseMvRxViewModel<D>, D : MvRxState,
        E : BaseMvRxViewModel<F>, F : MvRxState,
        G : BaseMvRxViewModel<H>, H : MvRxState,
        I
        > withRenderingState(
        viewModel1: A,
        viewModel2: C,
        viewModel3: E,
        viewModel4: G,
        block: (B, D, F, H) -> I
) = block(viewModel1.renderingState, viewModel2.renderingState, viewModel3.renderingState, viewModel4.renderingState)

/**
 * Access ViewModelState from five ViewModels asynchronously on merge thread
 */
fun <
        A : BaseMvRxViewModel<B>, B : MvRxState,
        C : BaseMvRxViewModel<D>, D : MvRxState,
        E : BaseMvRxViewModel<F>, F : MvRxState,
        G : BaseMvRxViewModel<H>, H : MvRxState,
        I : BaseMvRxViewModel<J>, J : MvRxState,
        K
        > withAsyncState(
        viewModel1: A,
        viewModel2: C,
        viewModel3: E,
        viewModel4: G,
        viewModel5: I,
        block: (B, D, F, H, J) -> K
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> viewModel3.withState { s3 -> viewModel4.withState { s4 -> viewModel5.withState { s5 -> block(s1, s2, s3, s4, s5) } } } } }

/**
 * Accesses ViewModel state from five ViewModels synchronously on main thread, and returns the result of the block.
 * This is only for UI rendering purpose to render the current state
 * For non-UI logic please use withAsyncState to avoid race condition
 */
fun <
        A : BaseMvRxViewModel<B>, B : MvRxState,
        C : BaseMvRxViewModel<D>, D : MvRxState,
        E : BaseMvRxViewModel<F>, F : MvRxState,
        G : BaseMvRxViewModel<H>, H : MvRxState,
        I : BaseMvRxViewModel<J>, J : MvRxState,
        K
        > withRenderingState(
        viewModel1: A,
        viewModel2: C,
        viewModel3: E,
        viewModel4: G,
        viewModel5: I,
        block: (B, D, F, H, J) -> K
) = block(viewModel1.renderingState, viewModel2.renderingState, viewModel3.renderingState, viewModel4.renderingState, viewModel5.renderingState)