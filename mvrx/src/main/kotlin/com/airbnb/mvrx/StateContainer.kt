package com.airbnb.mvrx

/**
 * Accesses ViewModel state from a single ViewModel synchronously and returns the result of the block.
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C> withState(viewModel1: A, block: (B) -> C) = viewModel1.withState { block(it) }

/**
 * Accesses ViewModel state from two ViewModels synchronously and returns the result of the block.
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState, E> withState(
    viewModel1: A,
    viewModel2: C,
    block: (B, D) -> E
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> block(s1, s2) } }

/**
 * Accesses ViewModel state from three ViewModels synchronously and returns the result of the block.
 */
fun <A : BaseMvRxViewModel<B>, B : MvRxState, C : BaseMvRxViewModel<D>, D : MvRxState, E : BaseMvRxViewModel<F>, F : MvRxState, G> withState(
    viewModel1: A,
    viewModel2: C,
    viewModel3: E,
    block: (B, D, F) -> G
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> viewModel3.withState { s3 -> block(s1, s2, s3) } } }

/**
 * Accesses ViewModel state from four ViewModels synchronously and returns the result of the block.
 */
fun <
    A : BaseMvRxViewModel<B>, B : MvRxState,
    C : BaseMvRxViewModel<D>, D : MvRxState,
    E : BaseMvRxViewModel<F>, F : MvRxState,
    G : BaseMvRxViewModel<H>, H : MvRxState,
    I
> withState(
    viewModel1: A,
    viewModel2: C,
    viewModel3: E,
    viewModel4: G,
    block: (B, D, F, H) -> I
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> viewModel3.withState { s3 -> viewModel4.withState { s4 -> block(s1, s2, s3, s4) } } } }

/**
 * Accesses ViewModel state from five ViewModels synchronously and returns the result of the block.
 */
fun <
        A : BaseMvRxViewModel<B>, B : MvRxState,
        C : BaseMvRxViewModel<D>, D : MvRxState,
        E : BaseMvRxViewModel<F>, F : MvRxState,
        G : BaseMvRxViewModel<H>, H : MvRxState,
        I : BaseMvRxViewModel<J>, J : MvRxState,
        K
        > withState(
        viewModel1: A,
        viewModel2: C,
        viewModel3: E,
        viewModel4: G,
        viewModel5: I,
        block: (B, D, F, H, J) -> K
) = viewModel1.withState { s1 -> viewModel2.withState { s2 -> viewModel3.withState { s3 -> viewModel4.withState { s4 -> viewModel5.withState { s5 -> block(s1, s2, s3, s4, s5) } } } } }
