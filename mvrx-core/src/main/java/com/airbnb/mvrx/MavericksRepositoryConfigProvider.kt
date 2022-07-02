package com.airbnb.mvrx

/**
 * Provide the [MavericksRepositoryConfig] for each new repository that is created.
 */
@ExperimentalMavericksApi
fun interface MavericksRepositoryConfigProvider<S : MavericksState> {
    fun provide(initialState: S): MavericksRepositoryConfig<S>
}
