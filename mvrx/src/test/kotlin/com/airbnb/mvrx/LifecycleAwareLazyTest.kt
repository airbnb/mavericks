package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LifecycleAwareLazyTest : BaseTest() {

    private lateinit var owner: TestLifecycleOwner
    lateinit var lazyProp: lifecycleAwareLazy<String>

    @Before
    fun setup() {
        owner = TestLifecycleOwner()
        lazyProp = lifecycleAwareLazy(owner) { "Hello World" }
    }

    @Test
    fun testNotInitializedBeforeOnCreate() {
        owner.lifecycle.markState(Lifecycle.State.INITIALIZED)
        assertFalse(lazyProp.isInitialized())
    }

    @Test
    fun testNotInitializedAfterOnCreate() {
        owner.lifecycle.markState(Lifecycle.State.INITIALIZED)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        assertTrue(lazyProp.isInitialized())
    }
}