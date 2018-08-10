package com.airbnb.mvrx.sample

import android.arch.lifecycle.Lifecycle
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.airbnb.mvrx.sample.views.BasicRow
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
abstract class BaseInstrumentationTest {
    protected val TAG = "MvRx"

    protected inline fun <reified A : AppCompatActivity> createTestRule() = ActivityTestRule(A::class.java)

    protected fun onViewWithText(text: String) = onView(ViewMatchers.withText(containsString(text)))

    protected fun ViewInteraction.click() = perform(ViewActions.click())

    protected fun scrollTo(text: String) = onView(instanceOf(RecyclerView::class.java))
            .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(allOf(instanceOf(BasicRow::class.java), hasDescendant(ViewMatchers.withText(text)))))

    protected fun scrollToAndClick(text: String) {
        scrollTo(text)
        onViewWithText(text).click()
    }

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