package com.airbnb.mvrx

/**
 * Accesses repository state from a single repository synchronously and returns the result of the block.
 */
fun <A : MavericksRepository<B>, B : MavericksState, C> withState(repository1: A, block: (B) -> C) = block(repository1.state)

/**
 * Accesses repository state from two repositories synchronously and returns the result of the block.
 */
fun <A : MavericksRepository<B>, B : MavericksState, C : MavericksRepository<D>, D : MavericksState, E> withState(
    repository1: A,
    repository2: C,
    block: (B, D) -> E
) = block(repository1.state, repository2.state)

/**
 * Accesses repository state from three repositories synchronously and returns the result of the block.
 */
fun <A : MavericksRepository<B>, B : MavericksState, C : MavericksRepository<D>, D : MavericksState, E : MavericksRepository<F>, F : MavericksState, G> withState(
    repository1: A,
    repository2: C,
    repository3: E,
    block: (B, D, F) -> G
) = block(repository1.state, repository2.state, repository3.state)

/**
 * Accesses repository state from four repositories synchronously and returns the result of the block.
 */
fun <
    A : MavericksRepository<B>, B : MavericksState,
    C : MavericksRepository<D>, D : MavericksState,
    E : MavericksRepository<F>, F : MavericksState,
    G : MavericksRepository<H>, H : MavericksState,
    I
    > withState(repository1: A, repository2: C, repository3: E, repository4: G, block: (B, D, F, H) -> I) =
    block(repository1.state, repository2.state, repository3.state, repository4.state)

/**
 * Accesses repository state from five repositories synchronously and returns the result of the block.
 */
fun <
    A : MavericksRepository<B>, B : MavericksState,
    C : MavericksRepository<D>, D : MavericksState,
    E : MavericksRepository<F>, F : MavericksState,
    G : MavericksRepository<H>, H : MavericksState,
    I : MavericksRepository<J>, J : MavericksState,
    K
    > withState(
    repository1: A,
    repository2: C,
    repository3: E,
    repository4: G,
    repository5: I,
    block: (B, D, F, H, J) -> K
) =
    block(repository1.state, repository2.state, repository3.state, repository4.state, repository5.state)
