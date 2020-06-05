package com.airbnb.mvrx

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class MvRxExtensionsKtTest : BaseTest() {
    @Test
    fun testParcelableIntoArgs() {
        val rect = Rect(1, 2, 3, 4)
        val args = rect.asMavericksArgs()
        assertEquals(rect, args.getParcelable<Rect>(MvRx.KEY_ARG))
    }

    @Test
    fun testSerializableIntoArgs() {
        val value = true
        val args = value.asMavericksArgs()
        assertEquals(true, args.getSerializable(MvRx.KEY_ARG))
    }
}