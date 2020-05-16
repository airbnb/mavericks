package com.airbnb.mvrx

import org.junit.Assert.assertEquals
import org.junit.Test

class ListExtensionsTest : BaseTest() {

    @Test
    fun testAppendNull() {
        val list = listOf(1, 2, 3)
        assertEquals(listOf(1, 2, 3), list.appendAt(null, 3))
    }

    @Test
    fun testAppendAtEnd() {
        val list = listOf(1, 2, 3)
        assertEquals(listOf(1, 2, 3, 4), list.appendAt(listOf(4), 3))
    }

    @Test
    fun testAppendPastEnd() {
        val list = listOf(1, 2, 3)
        assertEquals(listOf(1, 2, 3, 4), list.appendAt(listOf(4), 3))
    }

    @Test
    fun testAppendInMiddle() {
        val list = listOf(1, 2, 3)
        assertEquals(listOf(1, 2, 4), list.appendAt(listOf(4), 2))
    }

    @Test
    fun testAppendAtBeginning() {
        val list = listOf(1, 2, 3)
        assertEquals(listOf(4), list.appendAt(listOf(4), 0))
    }

    @Test
    fun testAppendAtShorterList() {
        val list = listOf(1)
        assertEquals(listOf(1, 4), list.appendAt(listOf(4), 3))
    }

    @Test
    fun testAppendSmallListInMiddleOfLongList() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(listOf(1, 4), list.appendAt(listOf(4), 1))
    }
}
