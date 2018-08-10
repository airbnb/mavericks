package com.airbnb.mvrx.sample.donotkeepactivities

import android.content.Intent
import com.airbnb.mvrx.sample.BaseInstrumentationTest
import com.airbnb.mvrx.sample.MainActivity
import org.junit.Rule
import org.junit.Test

class MainActivityTestsDoNotKeep : BaseInstrumentationTest() {

    @JvmField
    @Rule
    var activityRule = createTestRule<MainActivity>()

    @Test
    fun testPersistState() {
        onViewWithText("Flow").click()
        onViewWithText("50").click()
        val originalActivity = activityRule.activity
        val intent = Intent(activityRule.activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activityRule.activity.startActivity(intent)
        onViewWithText("Flow").displayed()
        originalActivity.waitForFinish()
        pressBack()
        onViewWithText("Count: 50").displayed()
        onViewWithText("Not persisted counter: 0").displayed()
    }
}