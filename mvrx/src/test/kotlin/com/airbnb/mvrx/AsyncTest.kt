package com.airbnb.mvrx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}