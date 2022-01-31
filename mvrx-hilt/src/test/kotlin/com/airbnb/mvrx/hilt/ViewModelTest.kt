package com.airbnb.mvrx.hilt

import com.airbnb.mvrx.test.MvRxTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltMavericksTestApplication_Application::class)
@RunWith(RobolectricTestRunner::class)
class ViewModelTest {

    @get:Rule
    val mvrxTestRule = MvRxTestRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var testViewModelFactory: TestViewModel.Factory

    var testViewModel: TestViewModel? = null

    @Before
    fun setUp() {
        hiltRule.inject()
        testViewModel = testViewModelFactory.create(TestState())
    }

    @After
    fun tearDown() {
        testViewModel = null
    }

    @Test
    fun test_view_model_injection() {
        assertEquals(true, testViewModel != null)
    }

    @Test
    fun test_view_model_initial_state() = runBlocking {
        val expected = ""

        val actual = testViewModel!!.stateFlow.first().data

        assertEquals(expected, actual)
    }

    @Test
    fun test_view_model_updates_state() = runBlocking {
        val expected = "Example Data"

        testViewModel!!.setData(expected)
        val actual = testViewModel!!.stateFlow.first().data

        assertEquals(expected, actual)
    }
}
