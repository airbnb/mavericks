package com.airbnb.mvrx.hellokoin

import com.airbnb.mvrx.test.MvRxTestRule
import com.airbnb.mvrx.withState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.ClassRule
import org.junit.Test

internal class ScopedHelloViewModelTest {

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule()
    }

    private val repo = mockk<HelloRepository> {
        every { sayHello() } returns Observable.just("Hello!")
    }

    private val initialState = ScopedHelloState()

    @Test
    fun `fetches message when created`() {
        val viewModel = ScopedHelloViewModel(initialState, repo, ScopedObject())

        verify(exactly = 1) { repo.sayHello() }

        withState(viewModel) { state ->
            assert(state.message() == "Hello!")
        }
    }

}