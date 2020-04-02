@file:Suppress("UNCHECKED_CAST")

package com.airbnb.mvrx

import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.collection.SparseArrayCompat
import org.junit.Test

class StateImmutabilityTest : BaseTest() {

    @Test()
    fun valProp() {
        data class State(val foo: Int = 5)
        State::class.assertImmutability()
    }

    @Test()
    fun immutableMap() {
        data class State(val foo: Map<String, Int> = mapOf("a" to 0))
        State::class.assertImmutability()
    }

    @Test()
    fun immutableList() {
        data class State(val foo: List<Int> = listOf(1, 2, 3))
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonDataState() {
        class State
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonDataStateWithComponent1() {
        class State {
            operator fun component1() = 5
        }
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonDataStateWithHashCode() {
        class State {
            override fun hashCode() = 123
        }
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonDataStateWithEquals() {
        class State {
            override fun equals(other: Any?) = false
        }
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun varState() {
        data class State(var foo: Int = 5)
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun mutableList() {
        data class State(val list: ArrayList<Int> = ArrayList())
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun mutableMap() {
        data class State(val map: HashMap<String, Int> = HashMap())
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun arrayMap() {
        data class State(val map: ArrayMap<String, Int> = ArrayMap())
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun sparseArray() {
        data class State(val map: SparseArray<Int> = SparseArray())
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun sparseArrayCompat() {
        data class State(val map: SparseArrayCompat<Int> = SparseArrayCompat())
        State::class.assertImmutability()
    }

    @Test(expected = IllegalArgumentException::class)
    fun lambda() {
        data class State(val func: () -> Unit = {})
        State::class.assertImmutability()
    }
}
