package com.airbnb.mvrx.mocking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher

fun testCoroutineScope() = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
