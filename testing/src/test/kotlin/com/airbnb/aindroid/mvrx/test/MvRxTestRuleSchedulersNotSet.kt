package com.airbnb.aindroid.mvrx.test

import org.junit.Test

class MvRxTestRuleSchedulersNotSet {
    /**
     * Can't subscribe until the main thread handler has been set.
     * This is done in the test rule.
     */
    @Test(expected = ExceptionInInitializerError::class)
    fun testImmediateSchedulersNotSet() {
        TestRuleViewModel()
    }
}