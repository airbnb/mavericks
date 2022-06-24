package com.airbnb.mvrx

import org.junit.Test

data class StateWithMutableMap(val map: MutableMap<String, String> = mutableMapOf()) : MavericksState
data class StateWithImmutableMap(val map: Map<String, String> = mapOf()) : MavericksState

class MutableStateValidationTest : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun mutableStateShouldFail() {
        class Repository(initialState: StateWithMutableMap) :
            TestMavericksRepository<StateWithMutableMap>(initialState) {

            fun addKeyToMap() {
                val myMap = withState(this) { it.map }
                myMap["foo"] = "bar"

                setState { copy(map = myMap) }
            }
        }
        Repository(StateWithMutableMap()).addKeyToMap()
    }

    @Test
    fun immutableStateShouldNotFail() {
        class Repository(initialState: StateWithImmutableMap) :
            TestMavericksRepository<StateWithImmutableMap>(initialState) {

            fun addKeyToMap() {
                val myMap = withState(this) { it.map }.toMutableMap()
                myMap["foo"] = "bar"

                setState { copy(map = myMap) }
            }
        }
        Repository(StateWithImmutableMap()).addKeyToMap()
    }
}
