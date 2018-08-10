package com.airbnb.mvrx.sample.keep

import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.filters.LargeTest
import android.support.v7.widget.RecyclerView
import com.airbnb.mvrx.sample.BaseInstrumentationTest
import com.airbnb.mvrx.sample.MainActivity
import com.airbnb.mvrx.sample.core.OkHttp3IdlingResource
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.android.get


@LargeTest
class MainActivityTests : BaseInstrumentationTest() {

    @JvmField
    @Rule
    var activityRule = createTestRule<MainActivity>()

    @Before
    fun setupIdleResource() {
        val resource: OkHttp3IdlingResource = activityRule.activity.get()
        IdlingRegistry.getInstance().register(resource)
    }

    @Test
    fun testHelloWorld() {
        onViewWithText("Hello World").click()
        onViewWithText("Hello World!").displayed()
    }

    @Test
    fun testDadJokes() {
        onViewWithText("Dad Jokes").click()
        onView(instanceOf(RecyclerView::class.java)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        onViewWithText("ID: ").displayed()
    }

    @Test
    fun testFlow() {
        onViewWithText("Flow").click()
        onViewWithText("50").click()
        onViewWithText("Count: 50").displayed()
    }

    @Test
    fun testPersistState() {
        onViewWithText("Flow").click()
        onViewWithText("50").click()
        onViewWithText("Count: 50").displayed()
        onViewWithText("Not persisted counter: 50").displayed()
        val intent = Intent(activityRule.activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activityRule.activity.startActivity(intent)
        onViewWithText("Flow").displayed()
        pressBack()
        onViewWithText("Count: 50").displayed()
        onViewWithText("Not persisted counter: 50").displayed()
    }
}