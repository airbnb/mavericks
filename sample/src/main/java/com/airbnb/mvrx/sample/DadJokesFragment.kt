package com.airbnb.mvrx.sample

import android.support.v4.app.FragmentActivity
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.appendAt
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.core.BaseMvRxFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.network.DadJokeService
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState
import org.koin.android.ext.android.inject

data class DadJokedState(
        /** We use this request to store the list of all jokes */
        val jokes: List<Joke> = emptyList(),
        /** We use this Async to keep track of the state of the current network request */
        val request: Async<List<Joke>> = Uninitialized
) : MvRxState

private const val JOKES_PER_PAGE = 7
class DadJokesViewModel(
        override val initialState: DadJokedState,
        private val dadJokeService: DadJokeService
) : MvRxViewModel<DadJokedState>() {

    init {
        fetchNextPage()
    }

    fun fetchNextPage() = withState { state ->
        if (!state.request.shouldLoad) return@withState

        dadJokeService
                .search(page = state.jokes.size / JOKES_PER_PAGE, limit = JOKES_PER_PAGE)
                .execute { copy(request = it, jokes = jokes.appendAt(it(), jokes.size)) }

    }

    companion object : MvRxViewModelFactory<DadJokedState> {
        override fun create(activity: FragmentActivity, state: DadJokedState): BaseMvRxViewModel<DadJokedState> {
            val service: DadJokeService by activity.inject()
            return DadJokesViewModel(state, service)
        }
    }
}

class DadJokesFragment : BaseMvRxFragment() {

    private val viewModel by fragmentViewModel(DadJokesViewModel::class)

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

        if (state.request is Loading) {
            basicRow {
                id("loading")
                title("Loading")
                onBind { _, _, _ -> viewModel.fetchNextPage() }
            }
        }
    }
}