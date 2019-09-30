package com.airbnb.mvrx.news

import com.airbnb.mvrx.news.models.HeadlinesResponse
import com.airbnb.mvrx.news.usecases.GetHeadlinesUseCase
import com.airbnb.mvrx.test.MvRxTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HeadlinesViewModelTest {

    @get:Rule val mvRxRule = MvRxTestRule()

    private lateinit var usecase: GetHeadlinesUseCase
    private lateinit var initialState: HeadlinesState
    private lateinit var viewModel: HeadlinesViewModel

    @Before
    fun setup() {
        usecase = mockk {
            every { getHeadlines(any()) } returns Single.fromCallable {
                HeadlinesResponse("200", 0, emptyList())
            }
        }
        initialState = HeadlinesState()
        viewModel = HeadlinesViewModel(initialState, usecase)
    }

    @Test
    fun `fetches headlines when created`() {
        verify(exactly = 1) { usecase.getHeadlines(1) }
    }

    @Test
    fun `does not fetch more results when there aren't any`() {
        viewModel.getMoreHeadlines()
        verify(exactly = 1) { usecase.getHeadlines(1) }
    }

}