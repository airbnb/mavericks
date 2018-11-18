package com.airbnb.mvrx.sample.features.dadjoke

import android.annotation.SuppressLint
import android.os.Parcelable
import com.airbnb.mvrx.*
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.core.simpleController
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.network.DadJokeService
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.loadingRow
import com.airbnb.mvrx.sample.views.marquee
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@SuppressLint("ParcelCreator")
@Parcelize
data class DadJokeDetailArgs(val id: String) : Parcelable

data class DadJokeDetailState(val joke: Async<Joke> = Uninitialized) : MvRxState

class DadJokeDetailViewModel @Inject constructor(
    private val dadJokeService: DadJokeService
) : MvRxViewModel<DadJokeDetailState>(DadJokeDetailState()) {

    fun fetchJoke(id: String) = withState { state ->
        if (!state.joke.shouldLoad) return@withState
        dadJokeService.fetch(id).execute { copy(joke = it) }
    }
}

class DadJokeDetailFragment : BaseFragment() {
    private val dadJokeDetailArgs by args<DadJokeDetailArgs>()
    private val viewModel: DadJokeDetailViewModel by fragmentViewModel(initializer = {
        it.fetchJoke(dadJokeDetailArgs.id)
    })

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
        }
    }
}