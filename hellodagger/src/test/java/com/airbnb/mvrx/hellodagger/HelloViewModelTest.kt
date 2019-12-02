package com.airbnb.mvrx.hellodagger

import com.airbnb.mvrx.withState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Test

internal class HelloViewModelTest {

    private val repo = mockk<HelloRepository> {
        every { sayHello() } returns Observable.just("Hello!")
    }

    private val initialState = HelloState()

    @Test
    fun `fetches message when created`() {
        val viewModel = HelloViewModel(initialState, repo)

        verify(exactly = 1) { repo.sayHello() }

        withState(viewModel) { state ->
            assert(state.message() == "Hello!")
        }
    }

}