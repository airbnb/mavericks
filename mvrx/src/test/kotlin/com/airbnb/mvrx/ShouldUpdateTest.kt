package com.airbnb.mvrx

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

data class ShouldUpdateState(val prop: Async<String> = Uninitialized, val prop2: String = "foo")

class ShouldUpdateTest : BaseTest() {

    private lateinit var store: MvRxStateStore<ShouldUpdateState>

    @Before
    fun setup() {
        store = MvRxStateStore(ShouldUpdateState())
    }

    @Test
    fun testShouldUpdateStartsWithNullOldState() {
        var callCount = 0
        val shouldUpdate: (ShouldUpdateState?, ShouldUpdateState) -> Boolean = { oldState, newState ->
            callCount++
            Assert.assertNull(oldState)
            Assert.assertEquals(ShouldUpdateState(), newState)
            true
        }
        store.subscribe(shouldUpdate = shouldUpdate) { _ -> Unit}
        Assert.assertEquals(1, callCount)
    }

    @Test
    fun testOnSuccess() {
        var callCount = 0
        store.set { copy(prop = Success("foo")) }
        store.subscribe(shouldUpdate = onSuccess(ShouldUpdateState::prop)) { state ->
            callCount++
            assertEquals(Success("foo"), state.prop)
        }
        assertEquals(1, callCount)
    }

    @Test
    fun testOnSuccessNoInitial() {
        var callCount = 0
        store.set { copy(prop = Success("foo")) }
        store.subscribe(shouldUpdate = onSuccessNoInitial(ShouldUpdateState::prop)) {
            callCount++
        }
        assertEquals(0, callCount)
    }

    @Test
    fun testOnFail() {
        var callCount = 0
        val fail = Fail<String>(IllegalStateException("foo"))
        store.set { copy(prop = fail) }
        store.subscribe(shouldUpdate = onFail(ShouldUpdateState::prop)) { state ->
            callCount++
            assertEquals(fail, state.prop)
        }
        assertEquals(1, callCount)
    }

    @Test
    fun testOnFailNoInitial() {
        var callCount = 0
        store.set { copy(prop = Fail(IllegalStateException("foo"))) }
        store.subscribe(shouldUpdate = onFailNoInitial(ShouldUpdateState::prop)) {
            callCount++
        }
        assertEquals(0, callCount)
    }

    @Test
    fun propertyWhitelistInitialValue() {
        var callCount = 0
        store.subscribe(shouldUpdate = propertyWhitelist(ShouldUpdateState::prop)) { state ->
            callCount++
            assertEquals(Uninitialized, state.prop)
        }
        assertEquals(1, callCount)
    }

    @Test
    fun propertyWhitelistNoInitialValue() {
        var callCount = 0
        store.subscribe(shouldUpdate = propertyWhitelistNoInitial(ShouldUpdateState::prop)) {
            callCount++
        }
        assertEquals(0, callCount)
    }

    @Test
    fun propertyWhitelistNoChange() {
        var callCount = 0
        store.subscribe(shouldUpdate = propertyWhitelist(ShouldUpdateState::prop2)) {
            callCount++
        }
        store.set { copy(prop2 = "foo") }
        assertEquals(1, callCount)
    }

    @Test
    fun propertyWhitelistWithChange() {
        var callCount = 0
        store.subscribe(shouldUpdate = propertyWhitelist(ShouldUpdateState::prop2)) {
            callCount++
        }
        store.set { copy(prop2 = "foo2") }
        assertEquals(2, callCount)
    }

    @Test
    fun propertyWhitelistSubscribeToDifferentProp() {
        var callCount = 0
        store.subscribe(shouldUpdate = propertyWhitelist(ShouldUpdateState::prop)) {
            callCount++
        }
        store.set { copy(prop2 = "foo2") }
        assertEquals(1, callCount)
    }
}