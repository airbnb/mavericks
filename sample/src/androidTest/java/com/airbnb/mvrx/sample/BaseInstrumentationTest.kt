package com.airbnb.mvrx.sample

import android.arch.lifecycle.Lifecycle
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.hamcrest.Matchers.containsString
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
abstract class BaseInstrumentationTest {
    protected val TAG = "MvRx"

    protected inline fun <reified A : AppCompatActivity> createTestRule() = ActivityTestRule(A::class.java)

    protected fun onViewWithText(text: String) = onView(ViewMatchers.withText(containsString(text)))

    protected fun ViewInteraction.click() = perform(ViewActions.click())

    protected fun ViewInteraction.displayed() = check(matches(isDisplayed()))

    protected fun pressBack() {
        Espresso.pressBack()
    }

    /**
     * Wait for the activity to finish since it doesn't happen immediately.
     */
    protected fun AppCompatActivity.waitForFinish() {
        val startedTime = System.currentTimeMillis()
        while (lifecycle.currentState != Lifecycle.State.DESTROYED) {
            Log.d(TAG, "Activity is in state ${lifecycle.currentState}")
            if (System.currentTimeMillis() - startedTime > 3000) throw IllegalStateException("Timed out waiting for activity to finish.")
        }
    }
}