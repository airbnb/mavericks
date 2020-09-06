package com.airbnb.mvrx

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MavericksMutabilityHelperKtTest {

    @Test
    fun isData() {
        assertTrue(TestDataClass::class.java.isData)
        assertFalse(String::class.java.isData)
    }

    data class TestDataClass(
            internal val foo: Int
    )
}

