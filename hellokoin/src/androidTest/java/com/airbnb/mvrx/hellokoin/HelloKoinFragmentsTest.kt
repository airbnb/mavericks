package com.airbnb.mvrx.hellokoin

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.filters.MediumTest
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.hellokoin.screens.hello.HelloFragment
import com.airbnb.mvrx.hellokoin.screens.scopedhello.ScopedHelloFragment
import com.airbnb.mvrx.withState
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.hamcrest.CoreMatchers.not
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@MediumTest
class HelloKoinFragmentsTest {

    companion object {
        val testScheduler = TestScheduler()

        @BeforeClass
        @JvmStatic
        fun setup() {
            RxJavaPlugins.reset()
            RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }
            RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
            RxJavaPlugins.setInitIoSchedulerHandler { testScheduler }
            RxJavaPlugins.setSingleSchedulerHandler { testScheduler }
        }
    }

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    val scenario: ActivityScenario<MainActivity>
        get() = activityScenarioRule.scenario

    @Test
    fun helloFragment_isInLoadingStateWhenCreated() {
        onView(withId(R.id.helloButton)).perform(click())

        scenario.onActivity { activity: MainActivity ->
            val fragment = activity.getCurrentFragment<HelloFragment>()
            withState(fragment.viewModel) {
                assert(it.message is Loading)
            }
        }

        onView(withId(R.id.messageTextView)).check(matches(withText(R.string.hello_fragment_loading_text)))
        onView(withId(R.id.helloButton)).check(matches(not(isEnabled())))
    }

    @Test
    fun scopedHelloFragment_isInLoadingStateWhenCreated() {
        onView(withId(R.id.scopedHelloButton)).perform(click())

        scenario.onActivity { activity: MainActivity ->
            val fragment = activity.getCurrentFragment<ScopedHelloFragment>()
            withState(fragment.viewModel) {
                assert(it.message is Loading)
            }
        }

        onView(withId(R.id.messageTextView)).check(matches(withText(R.string.hello_fragment_loading_text)))
        onView(withId(R.id.helloButton)).check(matches(not(isEnabled())))
    }

    @Test
    fun scopedHelloFragment_isInSuccessStateOnRestart() {
        onView(withId(R.id.scopedHelloButton)).perform(click())
        scenario.onActivity { activity -> activity.onBackPressed() }

        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        onView(withId(R.id.scopedHelloButton)).perform(click())

        scenario.onActivity { activity: MainActivity ->
            val fragment = activity.getCurrentFragment<ScopedHelloFragment>()
            withState(fragment.viewModel) {
                assert(it.message is Success)
            }
        }

        onView(withId(R.id.messageTextView)).check(matches(withText("Hello, world!")))
        onView(withId(R.id.helloButton)).check(matches(isEnabled()))
    }
}