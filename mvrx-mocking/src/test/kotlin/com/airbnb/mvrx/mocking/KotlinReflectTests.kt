package com.airbnb.mvrx.mocking

import org.junit.Test

class KotlinReflectTests {
    @Test(expected = IllegalArgumentException::class)
    fun mutableKotlinList() {
        data class State(val mutableList: MutableList<String>)
        assertMavericksDataClassImmutabilityWithKotlinReflect(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mutableKotlinMap() {
        data class State(val mutableMap: MutableMap<String, String>)
        assertMavericksDataClassImmutabilityWithKotlinReflect(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mutableKotlinSet() {
        data class State(val mutableSet: MutableSet<String>)
        assertMavericksDataClassImmutabilityWithKotlinReflect(State::class)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mutableProperty() {
        data class State(var foo: Int)
        assertMavericksDataClassImmutabilityWithKotlinReflect(State::class)
    }

    @Test
    fun readOnlyPropertyIsAllowed() {
        data class State(val foo: Int)
        assertMavericksDataClassImmutabilityWithKotlinReflect(State::class)
    }
}