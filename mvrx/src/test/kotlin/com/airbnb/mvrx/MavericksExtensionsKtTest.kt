package com.airbnb.mvrx

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class MavericksExtensionsKtTest : BaseTest() {
    @Test
    fun testParcelableIntoArgs() {
        val rect = Rect(1, 2, 3, 4)
        val args = rect.asMavericksArgs()
        assertEquals(rect, args.getParcelable<Rect>(Mavericks.KEY_ARG))
    }

    @Test
    fun testSerializableIntoArgs() {
        val value = true
        val args = value.asMavericksArgs()
        assertEquals(true, args.getSerializable(Mavericks.KEY_ARG))
    }
}