package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import com.airbnb.mvrx.MvRx.KEY_ARG
import org.junit.Assert.assertEquals
import kotlinx.android.parcel.Parcelize
import org.junit.Test
import java.io.Serializable

/** Test auto creating state from fragment arguments. */
class FragmentStateProviderTest : MvRxBaseTest() {

    @Test
    fun testParcelableArgs() {
        val args = ParcelableArgs("hello")
        val frag = Frag(args)
        val state: TestState = frag._fragmentViewModelInitialStateProvider()
        assertEquals(args.str, state.str)
        assertEquals(null, state.num)
    }

    @Test
    fun testSerializableArgs() {
        val args = SerializableArgs(7)
        val frag = Frag(args)
        val state: TestState = frag._fragmentViewModelInitialStateProvider()
        assertEquals(null, state.str)
        assertEquals(args.num, state.num)
    }

    @Test
    fun testLongArgs() {
        val frag = Frag(8L)
        val state: TestState = frag._fragmentViewModelInitialStateProvider()
        assertEquals("id", state.str)
        assertEquals(8, state.num)
    }

    @Test
    fun testNoArgs() {
        val frag = Frag(null)
        val state: TestState = frag._fragmentViewModelInitialStateProvider()
        assertEquals("empty", state.str)
        assertEquals(2, state.num)
    }

    @Test
    fun testNoMatchingArgsFallsbackToEmptyConstructor() {
        val frag = Frag("unknown arg type")
        val state: TestState = frag._fragmentViewModelInitialStateProvider()
        assertEquals("empty", state.str)
        assertEquals(2, state.num)
    }

    // TODO eli_hart: 5/29/18 fail once we don't publicly expose args
    //    @Test(expected = IllegalStateException::class)
    //    fun testFailsWithNoMatchingConstructor() {
    //        val frag = Frag("string")
    //        val state: TestState = frag._fragmentViewModelInitialStateProvider()
    //    }
}

class Frag<T>(args: T?) : Fragment() {
    init {
        arguments = Bundle().apply {
            when (args) {
                is Parcelable -> putParcelable(KEY_ARG, args)
                is Serializable -> putSerializable(KEY_ARG, args)
            }
        }
    }
}

data class TestState(
    val str: String?,
    val num: Int?
) : MvRxState {
    constructor(args: ParcelableArgs) : this(args.str, null)
    constructor(args: SerializableArgs) : this(null, args.num)
    constructor(id: Long) : this("id", id.toInt())
    constructor() : this("empty", 2)
}

@Parcelize
class ParcelableArgs(val str: String) : Parcelable

class SerializableArgs(val num: Int) : Serializable