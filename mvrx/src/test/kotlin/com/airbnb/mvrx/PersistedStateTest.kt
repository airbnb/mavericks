package com.airbnb.mvrx

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito

class PersistedStateTest : BaseTest() {

    @Suppress("unused")
    enum class MyEnum {
        A,
        B,
        C
    }

    @Parcelize
    data class ParcelableClass(val count: Int = 0) : Parcelable

    @Parcelize
    data class ParcelableClassWithPersistState(@PersistState val count: Int = 0) : Parcelable

    @Test
    fun saveDefaultInt() {
        data class State(@PersistState val count: Int = 5) : MavericksState

        val bundle = persistMavericksState(State())
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(5, newState.count)
    }

    data class StateWithInternalVal(@PersistState internal val count: Int = 5) : MavericksState

    @Test
    fun saveInternalInt() {
        // internal properties in data classes have different method names generated for them,
        // and our custom copy function needs to handle that.
        val bundle = persistMavericksState(StateWithInternalVal())
        val newState = restorePersistedMavericksState(bundle, StateWithInternalVal())
        assertEquals(5, newState.count)
    }

    @Test
    fun validatesMissingKeyInBundle() {
        val exception: IllegalStateException = assertThrows(
            IllegalStateException::class.java
        ) {
            data class State(@PersistState val count: Int = 5) : MavericksState

            val newState = restorePersistedMavericksState(Bundle(), State(), validation = true)
            assertEquals(5, newState.count)
        }
        assertEquals(
            "savedInstanceState bundle should have a key for state property at position 0 but it was missing.",
            // The mockito mock adds a random id string at the end.
            exception.message?.substringBeforeLast("$")
        )
    }

    @Test
    fun savePrivateDefaultInt() {
        data class State(@PersistState private val count: Int = 5) : MavericksState {
            fun exposeCount() = count
        }

        val bundle = persistMavericksState(State())
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(5, newState.exposeCount())
    }

    @Test
    fun saveSetInt() {
        data class State(@PersistState val count: Int = 0) : MavericksState

        val bundle = persistMavericksState(State(count = 7))
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(7, newState.count)
    }

    @Test
    fun saveSetIntWithSecondaryConstructor() {
        data class State(@PersistState val count: Int = 0) : MavericksState {
            @Suppress("unused")
            constructor(args: String) : this(args.toInt())
        }

        val bundle = persistMavericksState(State(count = 7))
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(7, newState.count)
    }

    @Test
    fun saveSetIntWithTwoPropertiesAndDerivedProp() {
        data class State(@PersistState val count: Int = 0, val name: String = "") : MavericksState {
            @Suppress("unused")
            val isEven = count % 2 == 0
        }

        val bundle = persistMavericksState(State(count = 7, name = "Gabriel"))
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(7, newState.count)
    }

    @Test
    fun saveSaveStateWithPrimitiveTypes() {
        data class State(
            @PersistState val intVal: Int = 0,
            @PersistState val longVal: Long = 0L,
            @PersistState val doubleVal: Double = 0.0,
            @PersistState val floatVal: Float = 0f,
            @PersistState val charVal: Char = 'A'
        ) : MavericksState

        val bundle = persistMavericksState(State(intVal = 1, longVal = 2, doubleVal = 3.0, floatVal = 4f, charVal = 'B'))
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(1, newState.intVal)
        assertEquals(2L, newState.longVal)
        assertEquals(3.0, newState.doubleVal, Double.MIN_VALUE)
        assertEquals(4f, newState.floatVal)
        assertEquals('B', newState.charVal)
    }

    @Test
    fun savePersistNothing() {
        data class State(val count1: Int = 0, val count2: Int = 0) : MavericksState

        val bundle = persistMavericksState(State(count1 = 7, count2 = 9))
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(0, newState.count1)
        assertEquals(0, newState.count2)
    }

    @Test
    fun savePersistParcelable() {
        data class State(val data: ParcelableClass = ParcelableClass()) : MavericksState

        val bundle = persistMavericksState(State())
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(0, newState.data.count)
    }

    @Test
    fun savePersistParcelableWithValue() {
        data class State(@PersistState val data: ParcelableClass = ParcelableClass()) : MavericksState

        val bundle = persistMavericksState(State(data = ParcelableClass(count = 5)))
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(5, newState.data.count)
    }

    @Test
    fun ignoreNestedPersistState() {
        data class State(@PersistState val data: ParcelableClassWithPersistState = ParcelableClassWithPersistState()) : MavericksState

        val bundle = persistMavericksState(State())
        val newState = restorePersistedMavericksState(bundle, State())
        assertEquals(0, newState.data.count)
    }

    @Test
    fun testNullableEnum() {
        data class State(@PersistState val data: MyEnum? = MyEnum.A) : MavericksState

        val bundle = persistMavericksState(State(data = null))
        val state = restorePersistedMavericksState(bundle, State())
        assertNull(state.data)
    }

    @Test
    fun testNullableEnumReversed() {
        data class State(@PersistState val data: MyEnum? = null) : MavericksState

        val bundle = persistMavericksState(State(data = MyEnum.A))
        val state = restorePersistedMavericksState(bundle, State())
        assertEquals(MyEnum.A, state.data)
    }

    @Test
    fun testParcelableList() {
        data class State2(@PersistState val data: List<ParcelableClass> = listOf(ParcelableClass(count = 2))) : MavericksState

        val bundle = persistMavericksState(State2())
        val state = restorePersistedMavericksState(bundle, State2())
        assertEquals(2, state.data[0].count)
    }

    @Test
    fun testParcelableListWithChangedValue() {
        data class State2(@PersistState val data: List<ParcelableClass> = listOf(ParcelableClass(count = 2))) : MavericksState

        val bundle = persistMavericksState(State2(listOf(ParcelableClass(3))))
        val state = restorePersistedMavericksState(bundle, State2())
        assertEquals(3, state.data[0].count)
    }

    @Test()
    fun testNonParcelableList() {
        val exception: IllegalStateException = assertThrows(
            IllegalStateException::class.java
        ) {
            data class State2(@PersistState val data: List<Context> = listOf(Mockito.mock(Context::class.java))) : MavericksState

            persistMavericksState(State2(), validation = true)
        }
        assertEquals(
            "Cannot parcel android.content.Context\$MockitoMock",
            // The mockito mock adds a random id string at the end.
            exception.message?.substringBeforeLast("$")
        )
    }

    @Test
    fun testParcelableSetWithChangedValue() {
        data class State2(@PersistState val data: Set<ParcelableClass> = setOf(ParcelableClass(count = 2))) : MavericksState

        val bundle = persistMavericksState(State2(setOf(ParcelableClass(3))))
        val state = restorePersistedMavericksState(bundle, State2())
        assertEquals(3, state.data.take(1).first().count)
    }

    @Test()
    fun testNonParcelableSet() {
        val exception: IllegalStateException = assertThrows(
            IllegalStateException::class.java
        ) {
            data class State2(@PersistState val data: Set<Context> = setOf(Mockito.mock(Context::class.java))) : MavericksState

            persistMavericksState(State2(), validation = true)
        }
        assertEquals(
            "Cannot parcel android.content.Context\$MockitoMock",
            // The mockito mock adds a random id string at the end.
            exception.message?.substringBeforeLast("$")
        )
    }

    @Test
    fun testParcelableMapWithChangedValue() {
        data class State2(
            @PersistState val data: Map<String, ParcelableClass> = mapOf("foo" to ParcelableClass(count = 2))
        ) : MavericksState

        val bundle = persistMavericksState(State2(mapOf("foo" to ParcelableClass(3))))
        val state = restorePersistedMavericksState(bundle, State2())
        assertEquals(3, state.data["foo"]?.count)
    }

    @Test()
    fun testNonParcelableMap() {
        val exception: IllegalStateException = assertThrows(
            IllegalStateException::class.java
        ) {
            data class State2(
                @PersistState val data: Map<String, Context> = mapOf("foo" to Mockito.mock(Context::class.java))
            ) : MavericksState

            persistMavericksState(State2(), validation = true)
        }
        assertEquals(
            "Cannot parcel android.content.Context\$MockitoMock",
            // The mockito mock adds a random id string at the end.
            exception.message?.substringBeforeLast("$")
        )
    }

    @Test()
    fun failOnNonParcelable() {
        val exception: IllegalStateException = assertThrows(
            IllegalStateException::class.java
        ) {
            class NonParcelableClass
            data class State(@PersistState val data: NonParcelableClass = NonParcelableClass()) : MavericksState
            persistMavericksState(State())
        }
        assertEquals(
            "Cannot persist '0': Class com.airbnb.mvrx.PersistedStateTest\$failOnNonParcelable\$exception\$1\$NonParcelableClass must be null, Serializable, or Parcelable.",
            exception.message
        )
    }

    @Test
    fun testClassWithExactly32Parameters() {
        data class StateWith32Params(
            val p0: Int = 0,
            @PersistState val p1: Int = 0,
            val p2: Int = 0,
            @PersistState val p3: Int = 0,
            val p4: Int = 0,
            @PersistState val p5: Int = 0,
            val p6: Int = 0,
            @PersistState val p7: Int = 0,
            val p8: Int = 0,
            @PersistState val p9: Int = 0,
            val p10: Int = 0,
            @PersistState val p11: Int = 0,
            val p12: Int = 0,
            @PersistState val p13: Int = 0,
            val p14: Int = 0,
            @PersistState val p15: Int = 0,
            val p16: Int = 0,
            @PersistState val p17: Int = 0,
            val p18: Int = 0,
            @PersistState val p19: Int = 0,
            val p20: Int = 0,
            @PersistState val p21: Int = 0,
            val p22: Int = 0,
            @PersistState val p23: Int = 0,
            val p24: Int = 0,
            @PersistState val p25: Int = 0,
            val p26: Int = 0,
            @PersistState val p27: Int = 0,
            val p28: Int = 0,
            @PersistState val p29: Int = 0,
            val p30: Int = 0,
            @PersistState val p31: Int = 0,
        ) : MavericksState

        val bundle = persistMavericksState(
            StateWith32Params(
                p0 = 1,
                p1 = 2,
                p30 = 0,
                p31 = 17,
            )
        )
        val newState = restorePersistedMavericksState(bundle, StateWith32Params())
        assertEquals(2, newState.p1)
        assertEquals(17, newState.p31)
        assertEquals(0, newState.p30)
        assertEquals(17, newState.p31)
    }

    @Test
    fun testClassWithMoreThan32Parameters() {
        data class StateWithLotsOfParameters(
            val p0: Int = 0,
            @PersistState val p1: Int = 0,
            val p2: Int = 0,
            @PersistState val p3: Int = 0,
            val p4: Int = 0,
            @PersistState val p5: Int = 0,
            val p6: Int = 0,
            @PersistState val p7: Int = 0,
            val p8: Int = 0,
            @PersistState val p9: Int = 0,
            val p10: Int = 0,
            @PersistState val p11: Int = 0,
            val p12: Int = 0,
            @PersistState val p13: Int = 0,
            val p14: Int = 0,
            @PersistState val p15: Int = 0,
            val p16: Int = 0,
            @PersistState val p17: Int = 0,
            val p18: Int = 0,
            @PersistState val p19: Int = 0,
            val p20: Int = 0,
            @PersistState val p21: Int = 0,
            val p22: Int = 0,
            @PersistState val p23: Int = 0,
            val p24: Int = 0,
            @PersistState val p25: Int = 0,
            val p26: Int = 0,
            @PersistState val p27: Int = 0,
            val p28: Int = 0,
            @PersistState val p29: Int = 0,
            val p30: Int = 0,
            @PersistState val p31: Int = 0,
            val p32: Int = 0,
            @PersistState val p33: Int = 0,
            val p34: Int = 0,
            @PersistState val p35: Int = 0,
            val p36: Int = 0,
            @PersistState val p37: Int = 0,
            val p38: Int = 0,
            @PersistState val p39: Int = 0
        ) : MavericksState

        val bundle = persistMavericksState(
            StateWithLotsOfParameters(
                p0 = 1,
                p1 = 2,
                p30 = 3,
                p31 = 4,
                p32 = 5,
                p33 = 6,
                p34 = 7,
                p35 = 8
            )
        )
        val newState = restorePersistedMavericksState(bundle, StateWithLotsOfParameters())
        assertEquals(2, newState.p1)
        assertEquals(4, newState.p31)
        assertEquals(6, newState.p33)
        assertEquals(8, newState.p35)
    }
}
