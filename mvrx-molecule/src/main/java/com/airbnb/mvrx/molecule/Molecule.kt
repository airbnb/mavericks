package com.airbnb.mvrx.molecule

import androidx.compose.runtime.Composable
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import com.airbnb.mvrx.MavericksRepository
import com.airbnb.mvrx.MavericksState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <S : MavericksState, T> MavericksRepository<S>.launchMoleculeCollection(
    recompositionClock: RecompositionClock = RecompositionClock.Immediate,
    moleculeBody: @Composable () -> T,
    reducer: S.(T) -> S
) {
    val flow = moleculeFlow(clock = recompositionClock, moleculeBody)

    _internalCoroutineScope.launch {
        flow.collectLatest { moleculeResult ->
            setStateSuspended {
                reducer(moleculeResult)
            }
        }
    }
}