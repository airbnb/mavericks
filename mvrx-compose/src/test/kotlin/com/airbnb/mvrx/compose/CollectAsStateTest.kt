package com.airbnb.mvrx.compose

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksViewModelConfigFactory
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class CollectAsStateTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(
            composeTestRule.activity,
            MavericksViewModelConfigFactory(composeTestRule.activity, storeContextOverride = Dispatchers.Unconfined)
        )
    }

    @Test
    fun collectAsStateWithMapperDoesNotLoopOrReevaluateOnUnrelatedRecomposition() {
        val mapperInvocations = AtomicInteger(0)
        val recompositions = AtomicInteger(0)
        lateinit var vm: CounterViewModel
        var lastMappedValue = ""

        composeTestRule.activity.setContent {
            val prefix = "count2="
            vm = mavericksViewModel()
            val count by vm.collectAsState(CounterState::count)
            val mapped by vm.collectAsState { state ->
                mapperInvocations.incrementAndGet()
                "$prefix${state.count2}"
            }
            lastMappedValue = mapped
            recompositions.incrementAndGet()
            Column {
                Text(text = "$count $mapped")
            }
        }

        composeTestRule.waitForIdle()
        assertEquals("count2=123", lastMappedValue)
        assertEquals(1, recompositions.get())
        // Mapper runs once for initialValue and once when the flow emits the initial state.
        assertEquals(2, mapperInvocations.get())

        mapperInvocations.set(0)
        recompositions.set(0)

        vm.incrementCount()
        composeTestRule.waitForIdle()

        assertEquals(1, recompositions.get())
        // Mapper runs once for the new state emission from the flow, not during composition.
        assertEquals(1, mapperInvocations.get())
    }

    @Test
    fun collectAsStateWithLifecycleWithMapperDoesNotLoopOrReevaluateOnUnrelatedRecomposition() {
        val mapperInvocations = AtomicInteger(0)
        val recompositions = AtomicInteger(0)
        lateinit var vm: CounterViewModel
        var lastMappedValue = ""

        composeTestRule.activity.setContent {
            val prefix = "count2="
            vm = mavericksViewModel()
            val count by vm.collectAsStateWithLifecycle(CounterState::count)
            val mapped by vm.collectAsStateWithLifecycle { state ->
                mapperInvocations.incrementAndGet()
                "$prefix${state.count2}"
            }
            lastMappedValue = mapped
            recompositions.incrementAndGet()
            Column {
                Text(text = "$count $mapped")
            }
        }

        composeTestRule.waitForIdle()
        assertEquals("count2=123", lastMappedValue)
        assertEquals(1, recompositions.get())
        assertEquals(2, mapperInvocations.get())

        mapperInvocations.set(0)
        recompositions.set(0)

        vm.incrementCount()
        composeTestRule.waitForIdle()

        assertEquals(1, recompositions.get())
        assertEquals(1, mapperInvocations.get())
    }
}
