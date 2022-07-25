@file:Suppress("UNCHECKED_CAST")

package com.airbnb.mvrx

import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.collection.SparseArrayCompat
import org.junit.Test

class StateImmutabilityTest : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun arrayMap() {
        data class State(val map: ArrayMap<String, Int> = ArrayMap())
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sparseArray() {
        data class State(val map: SparseArray<Int> = SparseArray())
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sparseArrayCompat() {
        data class State(val map: SparseArrayCompat<Int> = SparseArrayCompat())
        assertMavericksDataClassImmutability(State::class)
    }
}
