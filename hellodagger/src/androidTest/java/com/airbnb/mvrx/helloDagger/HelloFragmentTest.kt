package com.airbnb.mvrx.helloDagger

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.not
import org.junit.Test

class HelloFragmentTest {

    @Test
    fun showsLoadingMessageWhenCreated() {
        val scenario = launchFragmentInContainer<HelloFragment>()
        onView(withId(R.id.messageTextView)).check(matches(withText(R.string.helloFragmentLoadingText)))
        onView(withId(R.id.helloButton)).check(matches(not(isEnabled())))
    }

}