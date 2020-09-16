package com.airbnb.mvrx

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class TestLifecycleOwner : LifecycleOwner {

    private val _lifecycle = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = _lifecycle
}