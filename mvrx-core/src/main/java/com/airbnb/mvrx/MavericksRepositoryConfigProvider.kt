package com.airbnb.mvrx

/**
 * Provide the [MavericksRepositoryConfig] for each new repository that is created.
 */
@ExperimentalMavericksApi
interface MavericksRepositoryConfigProvider {
    operator fun <S : MavericksState> invoke(
        repository: MavericksRepository<S>,
        initialState: S
    ): MavericksRepositoryConfig<S>
}