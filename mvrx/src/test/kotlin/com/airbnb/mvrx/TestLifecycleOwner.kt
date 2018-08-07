package com.airbnb.mvrx

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry

class TestLifecycleOwner : LifecycleOwner {

    private val _lifecycle = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = _lifecycle
}