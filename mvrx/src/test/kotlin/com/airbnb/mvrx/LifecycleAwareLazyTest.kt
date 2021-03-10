package com.airbnb.mvrx

import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LifecycleAwareLazyTest : BaseTest() {

    private lateinit var owner: TestLifecycleOwner
    private lateinit var lazyProp: lifecycleAwareLazy<String>

    @Before
    fun setup() {
        owner = TestLifecycleOwner()
        lazyProp = lifecycleAwareLazy(owner) { "Hello World" }
    }

    @Test
    fun testNotInitializedBeforeOnCreate() {
        owner.lifecycle.currentState = Lifecycle.State.INITIALIZED
        assertFalse(lazyProp.isInitialized())
    }

    @Test
    fun testNotInitializedAfterOnCreate() {
        owner.lifecycle.currentState = Lifecycle.State.INITIALIZED
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        assertTrue(lazyProp.isInitialized())
    }

    @Test
    fun testInitializedIfAlreadyStarted() {
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        lazyProp = lifecycleAwareLazy(owner) { "Hello World" }
        assertTrue(lazyProp.isInitialized())
    }

    @Test
    fun testInitializedIfOnBackgroundThread() {
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        lazyProp = lifecycleAwareLazy(owner, { false }) { "Hello World" }
        assertTrue(lazyProp.isInitialized())
    }

    @Test
    fun testIsNotInitializedIfOnBackgroundThreadAndDestroyed() {
        // Lifecycle can't move from an INITIALIZED to DESTROYED state.
        // So, we need to first we move it into a created state.
        owner.lifecycle.currentState = Lifecycle.State.CREATED
        owner.lifecycle.currentState = Lifecycle.State.DESTROYED
        lazyProp = lifecycleAwareLazy(owner, { false }) { "Hello World" }
        assertFalse(lazyProp.isInitialized())
    }
}
