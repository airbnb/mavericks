package com.airbnb.mvrx

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class TestLifecycleOwner : LifecycleOwner {

    var observerAddedCount = 0

    private val _lifecycle = object : LifecycleRegistry(this) {
        override fun addObserver(observer: LifecycleObserver) {
            observerAddedCount += 1
            super.addObserver(observer)
        }
    }

    override fun getLifecycle(): LifecycleRegistry = _lifecycle
}
