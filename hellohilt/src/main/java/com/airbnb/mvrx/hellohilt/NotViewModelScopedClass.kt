package com.airbnb.mvrx.hellohilt

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class NotViewModelScopedClass @Inject constructor() {
    val id = instanceId.incrementAndGet()

    companion object {
        private val instanceId = AtomicInteger(0)
    }
}
