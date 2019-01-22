package com.airbnb.mvrx.sample.features.dadjoke

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.core.simpleController
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.network.DadJokeService
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.loadingRow
import com.airbnb.mvrx.sample.views.marquee
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_hello_world.*
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
        dadJokeService.random().subscribeOn(Schedulers.io()).execute { copy(joke = it) }
    }

    companion object : MvRxViewModelFactory<RandomDadJokeViewModel, RandomDadJokeState> {

        override fun create(viewModelContext: ViewModelContext, state: RandomDadJokeState): RandomDadJokeViewModel {
            val service: DadJokeService by viewModelContext.activity.inject()
            return RandomDadJokeViewModel(state, service)
        }
    }
}

class RandomDadJokeFragment : BaseFragment() {
    private val viewModel: RandomDadJokeViewModel by fragmentViewModel()

    override fun epoxyController() = simpleController(viewModel) { state ->
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
            return@simpleController
        }

        basicRow {
            id("joke")
            title(joke.joke)
            clickListener { _ -> viewModel.fetchRandomJoke() }
        }
    }
}