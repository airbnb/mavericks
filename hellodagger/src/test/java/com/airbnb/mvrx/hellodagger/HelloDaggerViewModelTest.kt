package com.airbnb.mvrx.hellodagger

import com.airbnb.mvrx.Success
import com.airbnb.mvrx.test.MvRxTestRule
import com.airbnb.mvrx.withState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

internal class HelloDaggerViewModelTest {

    @get:Rule
    val mvrxRule = MvRxTestRule()


    @Test
    fun `fetches message when created`()  {
        val repo = mockk<HelloRepository> {
            every { sayHello() } returns flowOf("Hello!")
        }
        val viewModel = HelloDaggerViewModel(HelloDaggerState(), repo)
        withState(viewModel) { state ->
            assertEquals(Success("Hello!"), state.message)
        }
    }
}