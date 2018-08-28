package com.airbnb.mvrx.sample.features.dadjoke

import android.annotation.SuppressLint
import android.os.Parcelable
import android.support.v4.app.FragmentActivity
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
import org.koin.android.ext.android.inject

@SuppressLint("ParcelCreator")
@Parcelize
data class DadJokeDetailArgs(val id: String) : Parcelable

data class DadJokeDetailState(val id: String, val joke: Async<Joke> = Uninitialized) : MvRxState {
    /**
     * This secondary constructor will automatically called if your Fragment has
     * a parcelable in its arguments at key [com.airbnb.mvrx.MvRx.KEY_ARG]
     */
    constructor(args: DadJokeDetailArgs) : this(id = args.id)
}

class DadJokeDetailViewModel(
    initialState: DadJokeDetailState,
    private val dadJokeService: DadJokeService
) : MvRxViewModel<DadJokeDetailState>(initialState) {

    init {
        fetchJoke()
    }

    private fun fetchJoke() = withState { state ->
        if (!state.joke.shouldLoad) return@withState
        dadJokeService.fetch(state.id).execute { copy(joke = it) }
    }

    companion object : MvRxViewModelFactory<DadJokeDetailState> {
        @JvmStatic
        override fun create(
            activity: FragmentActivity,
            state: DadJokeDetailState
        ): BaseMvRxViewModel<DadJokeDetailState> {
            val service: DadJokeService by activity.inject()
            return DadJokeDetailViewModel(state, service)
        }
    }
}

class DadJokeDetailFragment : BaseFragment() {
    private val viewModel: DadJokeDetailViewModel by fragmentViewModel()

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