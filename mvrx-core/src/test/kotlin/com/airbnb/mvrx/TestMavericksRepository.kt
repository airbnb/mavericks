package com.airbnb.mvrx

abstract class TestMavericksRepository<S : MavericksState>(initialState: S) : MavericksRepository<S>(
    initialState,
    object : MavericksRepositoryConfigProvider {
        override fun <S : MavericksState> invoke(repository: MavericksRepository<S>, initialState: S): MavericksRepositoryConfig<S> {
            return TestMavericksRepositoryConfig(initialState)
        }
    }
)
