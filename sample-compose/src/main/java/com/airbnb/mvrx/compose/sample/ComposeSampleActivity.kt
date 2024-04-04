package com.airbnb.mvrx.compose.sample

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
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
import com.airbnb.mvrx.compose.collectAsStateWithLifecycle
import com.airbnb.mvrx.compose.mavericksActivityViewModel
import com.airbnb.mvrx.compose.mavericksViewModel

data class CounterState(
    val count: Int = 0,
) : MavericksState {
    @Suppress("unused")
    constructor(arguments: ArgumentsTest) : this(count = arguments.count)
}

data class ArgumentsTest(val count: Int) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ArgumentsTest> {
        override fun createFromParcel(parcel: Parcel): ArgumentsTest {
            return ArgumentsTest(parcel)
        }

        override fun newArray(size: Int): Array<ArgumentsTest?> {
            return arrayOfNulls(size)
        }
    }
}

class CounterViewModel(initialState: CounterState) : MavericksViewModel<CounterState>(initialState) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

class ComposeSampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Column {
                Box(modifier = Modifier.weight(1f)) {
                    CounterScreenNavHost("Counter Screen in Nav Graph 1", useInitialArgument = false)
                }
                Divider()
                Box(modifier = Modifier.weight(1f)) {
                    CounterScreenNavHost("Counter Screen in Nav Graph 2", useInitialArgument = true)
                }
            }
        }
    }

    @Composable
    fun CounterScreenNavHost(title: String, useInitialArgument: Boolean) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = "counter") {
            composable("counter") {
                CounterScreen(title, useInitialArgument)
            }
        }
    }

    @Composable
    fun CounterScreen(title: String, useInitialArgument: Boolean) {
        // This will get or create a ViewModel scoped to the closest LocalLifecycleOwner which, in this case, is the NavHost.
        val navScopedViewModel: CounterViewModel = mavericksViewModel(argsFactory = { ArgumentsTest(5) }.takeIf { useInitialArgument })
        // This will get or create a ViewModel scoped to the Activity.
        val activityScopedViewModel: CounterViewModel = mavericksActivityViewModel()

        val navScopedCount by navScopedViewModel.collectAsState(CounterState::count)
        val activityScopedCount by activityScopedViewModel.collectAsStateWithLifecycle(CounterState::count)

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
