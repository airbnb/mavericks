package com.airbnb.mvrx.sample

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.onFail
import com.airbnb.mvrx.sample.core.BaseMvRxFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.models.JokesResponse
import com.airbnb.mvrx.sample.network.DadJokeService
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState
import org.koin.android.ext.android.inject

data class DadJokedState(
        /** We use this request to store the list of all jokes */
        val jokes: List<Joke> = emptyList(),
        /** We use this Async to keep track of the state of the current network request */
        val request: Async<JokesResponse> = Uninitialized
) : MvRxState

private const val JOKES_PER_PAGE = 5
class DadJokesViewModel(
        override val initialState: DadJokedState,
        private val dadJokeService: DadJokeService
) : MvRxViewModel<DadJokedState>() {

    init {
        fetchNextPage()
    }

    fun fetchNextPage() = withState { state ->
        if (state.request is Loading) return@withState

        dadJokeService
                .search(page = state.jokes.size / JOKES_PER_PAGE + 1, limit = JOKES_PER_PAGE)
                .execute { copy(request = it, jokes = jokes + (it()?.results ?: emptyList())) }

    }

    companion object : MvRxViewModelFactory<DadJokedState> {
        override fun create(activity: FragmentActivity, state: DadJokedState): BaseMvRxViewModel<DadJokedState> {
            val service: DadJokeService by activity.inject()
            return DadJokesViewModel(state, service)
        }
    }
}

private const val TAG = "DadJokesFragment"
class DadJokesFragment : BaseMvRxFragment() {

    private val viewModel by fragmentViewModel(DadJokesViewModel::class)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subscribe(onFail(DadJokedState::request)) {
            Snackbar.make(coordinatorLayout, "Jokes request failed.", Snackbar.LENGTH_INDEFINITE).show()
            Log.w(TAG, "Jokes request failed", (it.request as Fail<*>).error)
        }
    }

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("marquee")
            title("Dad Jokes")
        }

        state.jokes.forEach { joke ->
            basicRow {
                id(joke.id)
                title(joke.joke)
            }
        }

        basicRow {
            id("loading ${state.jokes.size}")
            title("Loading")
            onBind { _, _, _ -> viewModel.fetchNextPage() }
        }
    }
}