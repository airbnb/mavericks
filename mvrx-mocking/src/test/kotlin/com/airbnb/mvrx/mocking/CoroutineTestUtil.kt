@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.airbnb.mvrx.mocking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestCoroutineDispatcher

fun testCoroutineScope() = CoroutineScope(SupervisorJob() + TestCoroutineDispatcher())