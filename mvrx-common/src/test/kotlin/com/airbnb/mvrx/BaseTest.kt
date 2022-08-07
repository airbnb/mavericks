package com.airbnb.mvrx

import org.junit.AfterClass
import org.junit.BeforeClass

abstract class BaseTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun classSetUp() {
            MavericksTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES = true
        }

        @JvmStatic
        @AfterClass
        fun classTearDown() {
            MavericksTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES = false
        }
    }
}
