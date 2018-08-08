package com.airbnb.mvrx.sample.core

import android.view.View

/** Allows fragments to bind views via a delegate, store a reference to the delegate and clear it when the view is destroyed. */
interface ViewBinder {
    fun <V : View> findViewById(id: Int): V?
}