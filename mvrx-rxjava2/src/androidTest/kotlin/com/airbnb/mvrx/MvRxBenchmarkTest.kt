package com.airbnb.mvrx

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Semaphore


@RunWith(AndroidJUnit4::class)
class MvRxBenchmarkTest {

    private class TestLifecycleOwner : LifecycleOwner {

        private val _lifecycle = LifecycleRegistry(this)

        init {
            _lifecycle.currentState == Lifecycle.State.RESUMED
        }

        override fun getLifecycle(): LifecycleRegistry = _lifecycle
    }

    private data class CounterState(val count: Int = 0) : MvRxState
    private class CounterViewModel : BaseMvRxViewModel<CounterState>(CounterState(), debugMode = false) {

        val semaphore = Semaphore(0)

        init {
            subscribe {
                if (it.count == N) {
                    semaphore.release()
                }
            }
        }

        fun resetCount() = setState { copy(count = 0) }

        fun incrementCount() = setState { copy(count = count + 1) }
    }

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test(timeout = Long.MAX_VALUE)
    fun benchmarkSomeWork() {
        val viewModel = CounterViewModel()
        benchmarkRule.measureRepeated {
            viewModel.resetCount()
            repeat(N) {
                viewModel.incrementCount()
            }
            viewModel.semaphore.acquire()
        }
    }


    companion object {
        private const val N = 100_000
    }
}