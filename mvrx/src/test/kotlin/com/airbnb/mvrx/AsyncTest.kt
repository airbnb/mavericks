package com.airbnb.mvrx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import com.airbnb.mvrx.Async.Companion.getMetadata
import com.airbnb.mvrx.Async.Companion.setMetadata
import org.junit.Test

class AsyncTest : MvRxBaseTest() {

    @Test
    fun incompleteValueIsNull() {
        assertNull(Uninitialized())
    }

    @Test
    fun loadingValueIsNull() {
        assertNull(Loading<Int>()())
    }

    @Test
    fun failValueIsNull() {
        assertNull(Fail<Int>(Exception("foo"))())
    }

    @Test
    fun successValueIsCorrect() {
        assertEquals(5, Success(5)())
    }

    @Test
    fun successHasMetadata() {
        val success = Success(5)
        assertNull(success.getMetadata<String>())

        success.setMetadata("hi")
        assertEquals("hi", success.getMetadata())
    }
}