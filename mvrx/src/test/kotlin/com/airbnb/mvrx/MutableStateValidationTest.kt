package com.airbnb.mvrx

import org.junit.Test

data class StateWithMutableMap(val map: MutableMap<String, String> = mutableMapOf()) : MavericksState
data class StateWithImmutableMap(val map: Map<String, String> = mapOf()) : MavericksState

class MutableStateValidationTest : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun mutableStateShouldFail() {
        class ViewModel(initialState: StateWithMutableMap) :
            TestMavericksViewModel<StateWithMutableMap>(initialState) {

            fun addKeyToMap() {
                val myMap = withState(this) { it.map }
                myMap["foo"] = "bar"

                setState { copy(map = myMap) }
            }
        }
        ViewModel(StateWithMutableMap()).addKeyToMap()
    }

    @Test
    fun immutableStateShouldNotFail() {
        class ViewModel(initialState: StateWithImmutableMap) :
            TestMavericksViewModel<StateWithImmutableMap>(initialState) {

            fun addKeyToMap() {
                val myMap = withState(this) { it.map }.toMutableMap()
                myMap["foo"] = "bar"

                setState { copy(map = myMap) }
            }
        }
        ViewModel(StateWithImmutableMap()).addKeyToMap()
    }
}
