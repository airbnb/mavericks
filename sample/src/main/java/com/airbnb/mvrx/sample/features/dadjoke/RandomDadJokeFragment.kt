package com.airbnb.mvrx.sample.features.dadjoke

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.mocking.mockSingleViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.databinding.RandomDadJokeFragmentBinding
import com.airbnb.mvrx.sample.features.dadjoke.mocks.mockRandomDadJokeState
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.network.DadJokeService
import com.airbnb.mvrx.sample.utils.viewBinding
import com.airbnb.mvrx.withState
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject

data class RandomDadJokeState(val joke: Async<Joke> = Uninitialized) : MavericksState

class RandomDadJokeViewModel(
    initialState: RandomDadJokeState,
    private val dadJokeService: DadJokeService
) : MvRxViewModel<RandomDadJokeState>(initialState) {
    init {
        fetchRandomJoke()
    }

    fun fetchRandomJoke() {
        suspend {
            dadJokeService.random()
        }.execute(Dispatchers.IO) { copy(joke = it) }
    }

    companion object : MavericksViewModelFactory<RandomDadJokeViewModel, RandomDadJokeState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: RandomDadJokeState
        ): RandomDadJokeViewModel {
            val service: DadJokeService by viewModelContext.activity.inject()
            return RandomDadJokeViewModel(state, service)
        }
    }
}

class RandomDadJokeFragment : BaseFragment(R.layout.random_dad_joke_fragment) {
    private val binding: RandomDadJokeFragmentBinding by viewBinding()
    private val viewModel: RandomDadJokeViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setupWithNavController(findNavController())
        binding.randomizeButton.setOnClickListener {
            viewModel.fetchRandomJoke()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.loader.isVisible = state.joke is Loading
        binding.row.isVisible = state.joke is Success
        binding.row.setTitle(state.joke()?.joke)
    }

    override fun provideMocks() = mockSingleViewModel(
        viewModelReference = RandomDadJokeFragment::viewModel,
        defaultState = mockRandomDadJokeState,
        defaultArgs = null
    ) {
        stateForLoadingAndFailure { ::joke }
    }
}
