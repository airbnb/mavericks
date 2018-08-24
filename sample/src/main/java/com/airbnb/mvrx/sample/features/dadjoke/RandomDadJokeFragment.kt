package com.airbnb.mvrx.sample.features.dadjoke

import android.support.v4.app.FragmentActivity
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.network.DadJokeService
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.loadingRow
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState
import org.koin.android.ext.android.inject

data class RandomDadJokeState(val joke: Async<Joke> = Uninitialized) : MvRxState

class RandomDadJokeViewModel(
        initialState: RandomDadJokeState,
        private val dadJokeService: DadJokeService
) : MvRxViewModel<RandomDadJokeState>(initialState) {
    init {
        fetchRandomJoke()
    }

    fun fetchRandomJoke() {
        dadJokeService.random().execute { copy(joke = it) }
    }

    companion object : MvRxViewModelFactory<RandomDadJokeState> {
        @JvmStatic override fun create(activity: FragmentActivity, state: RandomDadJokeState): BaseMvRxViewModel<RandomDadJokeState> {
            val service: DadJokeService by activity.inject()
            return RandomDadJokeViewModel(state, service)
        }
    }
}

class RandomDadJokeFragment : BaseFragment() {
    private val viewModel : RandomDadJokeViewModel by fragmentViewModel()


    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("marquee")
            title("Dad Joke")
        }

        /**
         * Async overrides the invoke operator so we can just call it. It will return the value if
         * it is Success or null otherwise.
         */
        val joke = state.joke()
        if (joke == null) {
            loadingRow {
                id("loading")
            }
            return@withState
        }

        basicRow {
            id("joke")
            title(joke.joke)
            clickListener { _ -> viewModel.fetchRandomJoke() }
        }
    }
}