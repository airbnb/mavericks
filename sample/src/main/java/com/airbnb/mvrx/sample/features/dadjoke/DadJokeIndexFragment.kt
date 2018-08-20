package com.airbnb.mvrx.sample.features.dadjoke

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
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.models.JokesResponse
import com.airbnb.mvrx.sample.network.DadJokeService
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.loadingRow
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState
import org.koin.android.ext.android.inject

data class DadJokeIndexState(
        /** We use this request to store the list of all jokes */
        val jokes: List<Joke> = emptyList(),
        /** We use this Async to keep track of the state of the current network request */
        val request: Async<JokesResponse> = Uninitialized
) : MvRxState

private const val JOKES_PER_PAGE = 5

/**
 * initialState *must* be implemented as a constructor parameter.
 */
class DadJokeIndexViewModel(
        initialState: DadJokeIndexState,
        private val dadJokeService: DadJokeService
) : MvRxViewModel<DadJokeIndexState>(initialState) {

    init {
        fetchNextPage()
    }

    fun fetchNextPage() = withState { state ->
        if (state.request is Loading) return@withState

        dadJokeService
                .search(page = state.jokes.size / JOKES_PER_PAGE + 1, limit = JOKES_PER_PAGE)
                .execute { copy(request = it, jokes = jokes + (it()?.results ?: emptyList())) }

    }

    /**
     * If you implement MvRxViewModelFactory in your companion object, MvRx will use that to create
     * your ViewModel. You can use this to achieve constructor dependency injection with MvRx.
     *
     * @see MvRxViewModelFactory
     */
    companion object : MvRxViewModelFactory<DadJokeIndexState> {
        override fun create(activity: FragmentActivity, state: DadJokeIndexState): BaseMvRxViewModel<DadJokeIndexState> {
            val service: DadJokeService by activity.inject()
            return DadJokeIndexViewModel(state, service)
        }
    }
}

private const val TAG = "DadJokeIndexFragment"
class DadJokeIndexFragment : BaseFragment() {

    /**
     * This will get or create a new ViewModel scoped to this Fragment. It will also automatically
     * subscribe to all state changes and call [invalidate] which we have wired up to
     * call [buildModels] in [BaseFragment].
     */
    private val viewModel by fragmentViewModel(DadJokeIndexViewModel::class)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * Use viewModel.subscribe to listen for changes. The parameter is a shouldUpdate
         * function that is given the old state and new state and returns whether or not to
         * call the subscriber. onSuccess, onFail, and propertyWhitelist ship with MvRx.
         */
        viewModel.subscribe(onFail(DadJokeIndexState::request)) {
            Snackbar.make(coordinatorLayout, "Jokes request failed.", Snackbar.LENGTH_INDEFINITE).show()
            Log.w(TAG, "Jokes request failed", (it.request as Fail<*>).error)
        }

        /**
         * This is similar to subscribe above but is given the old and new state.
         * Returning early from this block is similar to using a shouldUpdate parameter.
         */
        viewModel.subscribeWithHistory { oldState, newState ->
            Log.d(TAG, "There were ${oldState?.jokes?.size} jokes and now there are ${newState.jokes.size} jokes.")
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
                clickListener { _ -> navigateTo(R.id.action_dadJokeIndex_to_dadJokeDetailFragment, DadJokeDetailArgs(joke.id)) }
            }
        }

        loadingRow {
            // Changing the ID will force it to rebind when new data is loaded even if it is
            // still on screen which will ensure that we trigger loading again.
            id("loading${state.jokes.size}")
            onBind { _, _, _ -> viewModel.fetchNextPage() }
        }
    }
}