@file:Suppress("UNCHECKED_CAST")

package com.airbnb.mvrx

import org.junit.Test

class StateImmutabilityTest : BaseTest() {

    @Test
    fun valProp() {
        data class State(val foo: Int = 5)
        assertMavericksDataClassImmutability(State::class)
    }

    @Test
    fun immutableMap() {
        data class State(val foo: Map<String, Int> = mapOf("a" to 0))
        assertMavericksDataClassImmutability(State::class)
    }

    @Test
    fun immutableList() {
        data class State(val foo: List<Int> = listOf(1, 2, 3))
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonDataState() {
        class State
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonDataStateWithComponent1() {
        class State {
            operator fun component1() = 5
        }
        assertMavericksDataClassImmutability(State::class)
    }

    @Suppress("EqualsOrHashCode")
    @Test(expected = IllegalArgumentException::class)
    fun nonDataStateWithHashCode() {
        class State {
            override fun hashCode() = 123
        }
        assertMavericksDataClassImmutability(State::class)
    }

    @Suppress("EqualsOrHashCode")
    @Test(expected = IllegalArgumentException::class)
    fun nonDataStateWithEquals() {
        class State {
            override fun equals(other: Any?) = false
        }
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun varState() {
        data class State(var foo: Int = 5)
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mutableList() {
        data class State(val list: ArrayList<Int> = ArrayList())
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mutableMap() {
        data class State(val map: HashMap<String, Int> = HashMap())
        assertMavericksDataClassImmutability(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun lambda() {
        data class State(val func: () -> Unit = {})
        assertMavericksDataClassImmutability(State::class)
    }

    @Test
    fun lambdaAllowed() {
        data class State(val func: () -> Unit = {})
        assertMavericksDataClassImmutability(State::class, allowFunctions = true)
    }
}
