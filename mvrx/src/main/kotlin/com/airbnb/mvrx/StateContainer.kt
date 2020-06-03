package com.airbnb.mvrx

/**
 * Accesses ViewModel state from a single ViewModel synchronously and returns the result of the block.
 */
fun <A : MavericksViewModel<B>, B : MvRxState, C> withState(viewModel1: A, block: (B) -> C) = block(viewModel1.state)

/**
 * Accesses ViewModel state from two ViewModels synchronously and returns the result of the block.
 */
fun <A : MavericksViewModel<B>, B : MvRxState, C : MavericksViewModel<D>, D : MvRxState, E> withState(
    viewModel1: A,
    viewModel2: C,
    block: (B, D) -> E
) = block(viewModel1.state, viewModel2.state)

/**
 * Accesses ViewModel state from three ViewModels synchronously and returns the result of the block.
 */
fun <A : MavericksViewModel<B>, B : MvRxState, C : MavericksViewModel<D>, D : MvRxState, E : MavericksViewModel<F>, F : MvRxState, G> withState(
    viewModel1: A,
    viewModel2: C,
    viewModel3: E,
    block: (B, D, F) -> G
) = block(viewModel1.state, viewModel2.state, viewModel3.state)

/**
 * Accesses ViewModel state from four ViewModels synchronously and returns the result of the block.
 */
fun <
    A : MavericksViewModel<B>, B : MvRxState,
    C : MavericksViewModel<D>, D : MvRxState,
    E : MavericksViewModel<F>, F : MvRxState,
    G : MavericksViewModel<H>, H : MvRxState,
    I
    > withState(viewModel1: A, viewModel2: C, viewModel3: E, viewModel4: G, block: (B, D, F, H) -> I) =
    block(viewModel1.state, viewModel2.state, viewModel3.state, viewModel4.state)

/**
 * Accesses ViewModel state from five ViewModels synchronously and returns the result of the block.
 */
fun <
    A : MavericksViewModel<B>, B : MvRxState,
    C : MavericksViewModel<D>, D : MvRxState,
    E : MavericksViewModel<F>, F : MvRxState,
    G : MavericksViewModel<H>, H : MvRxState,
    I : MavericksViewModel<J>, J : MvRxState,
    K
    > withState(viewModel1: A, viewModel2: C, viewModel3: E, viewModel4: G, viewModel5: I, block: (B, D, F, H, J) -> K) =
    block(viewModel1.state, viewModel2.state, viewModel3.state, viewModel4.state, viewModel5.state)
