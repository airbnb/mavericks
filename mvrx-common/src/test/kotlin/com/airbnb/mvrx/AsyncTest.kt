package com.airbnb.mvrx

import com.airbnb.mvrx.Async.Companion.getMetadata
import com.airbnb.mvrx.Async.Companion.setMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AsyncTest : BaseTest() {

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

    @Test
    fun testFailEqualsWithSameException() {
        val fail1 = Fail<Int>(IllegalStateException("foo"))
        val fail2 = Fail<Int>(IllegalStateException("foo").withStackTraceOf(fail1.error))
        assertEquals(fail1, fail2)
    }

    @Test
    fun testFailNotEqualsWithDifferentMessage() {
        val fail1 = Fail<Int>(IllegalStateException("foo"))
        val fail2 = Fail<Int>(IllegalStateException("foo2").withStackTraceOf(fail1.error))
        assertNotEquals(fail1, fail2)
    }

    @Test
    fun testFailNotEqualsWithDifferentClass() {
        val fail1 = Fail<Int>(IllegalStateException("foo"))
        val fail2 = Fail<Int>(IllegalArgumentException("foo").withStackTraceOf(fail1.error))
        assertNotEquals(fail1, fail2)
    }

    @Test
    fun testFailNotEqualsWithDifferentStackTrace() {
        val fail1 = Fail<Int>(IllegalStateException("foo"))
        val fail2 = Fail<Int>(IllegalStateException("foo"))
        assertNotEquals(fail1, fail2)
    }

    @Test
    fun testFailNotEqualsWhenOneHasNoStackTrace() {
        val fail1 = Fail<Int>(IllegalStateException("foo").apply { stackTrace = emptyArray() })
        val fail2 = Fail<Int>(IllegalStateException("foo"))
        assertNotEquals(fail1, fail2)
    }

    @Test
    fun testFailNotEqualsWhenOtherHasNoStackTrace() {
        val fail1 = Fail<Int>(IllegalStateException("foo"))
        val fail2 = Fail<Int>(IllegalStateException("foo").apply { stackTrace = emptyArray() })
        assertNotEquals(fail1, fail2)
    }

    @Test
    fun testFailEqualsWhenNeitherHasStackTrace() {
        val fail1 = Fail<Int>(IllegalStateException("foo").apply { stackTrace = emptyArray() })
        val fail2 = Fail<Int>(IllegalStateException("foo").apply { stackTrace = emptyArray() })
        assertEquals(fail1, fail2)
    }

    private fun Throwable.withStackTraceOf(other: Throwable): Throwable {
        stackTrace = other.stackTrace
        return this
    }
}
