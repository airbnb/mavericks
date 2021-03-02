package com.airbnb.mvrx.compose.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksActivityViewModel
import com.airbnb.mvrx.compose.mavericksViewModel

data class CounterState(
    val count: Int = 0,
) : MavericksState

class CounterViewModel(initialState: CounterState) : MavericksViewModel<CounterState>(initialState) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

class ComposeSampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Column {
                Box(modifier = Modifier.weight(1f)) {
                    CounterScreenNavHost("Counter Screen in Nav Graph 1")
                }
                Divider()
                Box(modifier = Modifier.weight(1f)) {
                    CounterScreenNavHost("Counter Screen in Nav Graph 2")
                }
            }
        }
    }

    @Composable
    fun CounterScreenNavHost(title: String) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = "counter") {
            composable("counter") {
                CounterScreen(title)
            }
        }
    }

    @Composable
    fun CounterScreen(title: String) {
        // This will get or create a ViewModel scoped to the closest LocalLifecycleOwner which, in this case, is the NavHost.
        val navScopedViewModel: CounterViewModel = mavericksViewModel()
        // This will get or create a ViewModel scoped to the Activity.
        val activityScopedViewModel: CounterViewModel = mavericksActivityViewModel()

        val navScopedCount by navScopedViewModel.collectAsState(CounterState::count)
        val activityScopedCount by activityScopedViewModel.collectAsState(CounterState::count)

        Column {
            Text(title)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Navigation Scoped Count: $navScopedCount\nActivity Scoped Count: $activityScopedCount")
            Spacer(modifier = Modifier.height(16.dp))
            IncrementNavigationCountButton()
            Spacer(modifier = Modifier.height(16.dp))
            IncrementActivityCountButton()
        }
    }

    @Composable
    fun IncrementNavigationCountButton() {
        val navScopedViewModel: CounterViewModel = mavericksViewModel()
        Button(onClick = navScopedViewModel::incrementCount) {
            Text("Increment Navigation Scoped Count")
        }
    }

    @Composable
    fun IncrementActivityCountButton() {
        val activityScopedViewModel: CounterViewModel = mavericksActivityViewModel()
        Button(onClick = activityScopedViewModel::incrementCount) {
            Text("Increment Activity Scoped Count")
        }
    }
}
