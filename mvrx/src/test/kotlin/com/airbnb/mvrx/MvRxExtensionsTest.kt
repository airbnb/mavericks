package com.airbnb.mvrx

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.junit.*
import org.junit.Assert.*

class MvRxExtensionsTest {

  @Parcelize
  data class Arg(val value: String) : Parcelable

  class DemoState1(val arg: Parcelable) : MvRxState

  class DemoState2(val arg: Arg) : MvRxState

  @Test
  fun testInitialDemoState1() {
    val state = _initialStateProvider(DemoState1::class.java, Arg("Hello"))
    assertEquals("Hello", (state.arg as Arg).value)
  }

  @Test
  fun testInitialDemoState2() {
    val state = _initialStateProvider(DemoState2::class.java, Arg("Hello"))
    assertEquals("Hello", state.arg.value)
  }
}
