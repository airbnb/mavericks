package com.airbnb.mvrx.sample.dadjoke

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.*
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.app.MvRxViewModel
import com.airbnb.mvrx.sample.dadjoke.models.Joke
import com.airbnb.mvrx.sample.core.views.*
import com.airbnb.mvrx.sample.dadjoke.network.DadJokeHomeService
import com.airbnb.mvrx.sample.dadjoke.network.DadJokeService
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject

data class DadJokeHomeState(val joke: Async<Joke> = Uninitialized) : MvRxState


class DadJokeHomeViewModel(
        initialState: DadJokeHomeState,
        private val dadJokeService: DadJokeHomeService
) : MvRxViewModel<DadJokeHomeState>(initialState) {

    init {
        fetchRandomJoke()
    }

    fun fetchRandomJoke() {
        dadJokeService.random().subscribeOn(Schedulers.io()).execute { copy(joke = it) }
    }

    companion object : MvRxViewModelFactory<DadJokeHomeViewModel, DadJokeHomeState> {

        override fun create(viewModelContext: ViewModelContext, state: DadJokeHomeState): DadJokeHomeViewModel {
            val service: DadJokeHomeService by viewModelContext.activity.inject()
            return DadJokeHomeViewModel(state, service)
        }
    }
}

private val loadDependencies by lazy { installHome() }
private fun installDependencies() = loadDependencies

class DadeJokeHomeFragment : BaseMvRxFragment() {

    private val viewModel: DadJokeHomeViewModel by fragmentViewModel()
    private lateinit var basicRow: BasicRow
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installDependencies()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_random_joke, container, false).apply {
                basicRow = findViewById(R.id.random_joke_row)
            }

    override fun invalidate() = withState(viewModel) { state ->
        val joke = state.joke()

        if (joke == null) {
            basicRow.setTitle("Loading mas..")
            return@withState
        }
        basicRow.setTitle(joke?.joke)
        basicRow.setSubtitle("click to see more")
        basicRow.setClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_main_to_dadJokeIndex)
        })
    }
}
