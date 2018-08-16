package com.airbnb.mvrx

import android.content.Context
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito

class PersistedStateTest : BaseTest() {

    enum class MyEnum { A,
        B,
        C
    }

    @Parcelize
    data class ParcelableClass(val count: Int = 0) : Parcelable

    @Parcelize
    data class ParcelableClassWithPersistState(@PersistState val count: Int = 0) : Parcelable

    @Test
    fun saveDefaultInt() {
        data class State(@PersistState val count: Int = 5)

        val bundle = PersistStateTestHelpers.persistState(State())
        val newState = PersistStateTestHelpers.restorePersistedState(bundle, State())
        Assert.assertEquals(5, newState.count)
    }

    @Test
    fun saveSetInt() {
        data class State(@PersistState val count: Int = 0)

        val bundle = PersistStateTestHelpers.persistState(State(count = 7))
        val newState = PersistStateTestHelpers.restorePersistedState(bundle, State())
        Assert.assertEquals(7, newState.count)
    }

    @Test
    fun savePersistNothing() {
        data class State(val count1: Int = 0, val count2: Int = 0)

        val bundle = PersistStateTestHelpers.persistState(State(count1 = 7, count2 = 9))
        val newState = PersistStateTestHelpers.restorePersistedState(bundle, State())
        Assert.assertEquals(0, newState.count1)
        Assert.assertEquals(0, newState.count2)
    }

    @Test
    fun savePersistParcelable() {
        data class State(val data: ParcelableClass = ParcelableClass())

        val bundle = PersistStateTestHelpers.persistState(State())
        val newState = PersistStateTestHelpers.restorePersistedState(bundle, State())
        Assert.assertEquals(0, newState.data.count)
    }

    @Test
    fun savePersistParcelableWithValue() {
        data class State(@PersistState val data: ParcelableClass = ParcelableClass())

        val bundle = PersistStateTestHelpers.persistState(State(data = ParcelableClass(count = 5)))
        val newState = PersistStateTestHelpers.restorePersistedState(bundle, State())
        Assert.assertEquals(5, newState.data.count)
    }

    @Test
    fun ignoreNestedPersistState() {
        data class State(@PersistState val data: ParcelableClassWithPersistState = ParcelableClassWithPersistState())

        val bundle = PersistStateTestHelpers.persistState(State())
        val newState = PersistStateTestHelpers.restorePersistedState(bundle, State())
        Assert.assertEquals(0, newState.data.count)
    }

    @Test
    fun testNullableEnum() {
        data class State(@PersistState val data: MyEnum? = MyEnum.A)

        val bundle = PersistStateTestHelpers.persistState(State(data = null))
        val state = PersistStateTestHelpers.restorePersistedState(bundle, State())
        assertNull(state.data)
    }

    @Test
    fun testNullableEnumReversed() {
        data class State(@PersistState val data: MyEnum? = null)

        val bundle = PersistStateTestHelpers.persistState(State(data = MyEnum.A))
        val state = PersistStateTestHelpers.restorePersistedState(bundle, State())
        assertEquals(MyEnum.A, state.data)
    }

    @Test
    fun testParcelableList() {
        data class State2(@PersistState val data: List<ParcelableClass> = listOf(ParcelableClass(count = 2)))

        val bundle = PersistStateTestHelpers.persistState(State2())
        val state = PersistStateTestHelpers.restorePersistedState(bundle, State2())
        assertEquals(2, state.data[0].count)
    }

    @Test
    fun testParcelableListWithChangedValue() {
        data class State2(@PersistState val data: List<ParcelableClass> = listOf(ParcelableClass(count = 2)))

        val bundle = PersistStateTestHelpers.persistState(State2(listOf(ParcelableClass(3))))
        val state = PersistStateTestHelpers.restorePersistedState(bundle, State2())
        assertEquals(3, state.data[0].count)
    }
    @Test(expected = IllegalStateException::class)
    fun testNonParcelableList() {
        data class State2(@PersistState val data: List<Context> = listOf(Mockito.mock(Context::class.java)))

        PersistStateTestHelpers.persistState(State2())
    }

    @Test
    fun testParcelableSetWithChangedValue() {
        data class State2(@PersistState val data: Set<ParcelableClass> = setOf(ParcelableClass(count = 2)))

        val bundle = PersistStateTestHelpers.persistState(State2(setOf(ParcelableClass(3))))
        val state = PersistStateTestHelpers.restorePersistedState(bundle, State2())
        assertEquals(3, state.data.take(1).first().count)
    }
    @Test(expected = IllegalStateException::class)
    fun testNonParcelableSet() {
        data class State2(@PersistState val data: Set<Context> = setOf(Mockito.mock(Context::class.java)))

        PersistStateTestHelpers.persistState(State2())
    }

    @Test
    fun testParcelableMapWithChangedValue() {
        data class State2(@PersistState val data: Map<String, ParcelableClass> = mapOf("foo" to ParcelableClass(count = 2)))

        val bundle = PersistStateTestHelpers.persistState(State2(mapOf("foo" to ParcelableClass(3))))
        val state = PersistStateTestHelpers.restorePersistedState(bundle, State2())
        assertEquals(3, state.data["foo"]?.count)
    }
    @Test(expected = IllegalStateException::class)
    fun testNonParcelableMap() {
        data class State2(@PersistState val data: Map<String, Context> = mapOf("foo" to Mockito.mock(Context::class.java)))

        PersistStateTestHelpers.persistState(State2())
    }

    @Test(expected = IllegalStateException::class)
    fun failOnInvalidState() {
        data class State(@PersistState val count1: Int, val count2: Int)

        val bundle = PersistStateTestHelpers.persistState(State(count1 = 5, count2 = 9))
        PersistStateTestHelpers.restorePersistedState(bundle, State::class.java)
    }

    @Test(expected = IllegalStateException::class)
    fun failOnNonParcelable() {
        class NonParcelableClass
        data class State(@PersistState val data: NonParcelableClass = NonParcelableClass())
        PersistStateTestHelpers.persistState(State())
    }

    @Test(expected = IllegalStateException::class)
    fun failNonOptional() {
        data class State(@PersistState val count: Int = 0, val count2: Int)
        val bundle = PersistStateTestHelpers.persistState(FactoryState())
        PersistStateTestHelpers.restorePersistedState(bundle, State::class.java)
    }
}