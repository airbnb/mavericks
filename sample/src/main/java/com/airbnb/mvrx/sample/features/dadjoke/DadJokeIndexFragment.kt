package com.airbnb.mvrx.sample.features.dadjoke

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.mocking.mockSingleViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.databinding.DadJokeIndexFragmentBinding
import com.airbnb.mvrx.sample.features.dadjoke.mocks.mockDadJokeIndexState
import com.airbnb.mvrx.sample.utils.viewBinding
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.loadingRow
import com.airbnb.mvrx.withState
import com.google.android.material.snackbar.Snackbar

class DadJokeIndexFragment : BaseFragment(R.layout.dad_joke_index_fragment) {
    private val binding: DadJokeIndexFragmentBinding by viewBinding()
    private val viewModel: DadJokeIndexViewModel by fragmentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Unique only will prevent the snackbar from showing again if the user rotates the screen or returns to this fragment.
        viewModel.onAsync(
            DadJokeIndexState::request, uniqueOnly(),
            onFail = {
                Snackbar.make(binding.root, "Jokes request failed.", Snackbar.LENGTH_INDEFINITE).show()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setupWithNavController(findNavController())
        binding.recyclerView.buildModelsWith(object : EpoxyRecyclerView.ModelBuilderCallback {
            override fun buildModels(controller: EpoxyController) {
                controller.buildModels()
            }
        })
    }

    override fun invalidate() {
        binding.recyclerView.requestModelBuild()
    }

    private fun EpoxyController.buildModels() = withState(viewModel) { state ->
        state.jokes.forEach { joke ->
            basicRow {
                id(joke.id)
                title(joke.joke)
            }
        }

        loadingRow {
            // Changing the ID will force it to rebind when new data is loaded even if it is
            // still on screen which will ensure that we trigger loading again.
            id("loading${state.jokes.size}")
            onBind { _, _, _ -> viewModel.fetchNextPage() }
        }
    }

    override fun provideMocks() = mockSingleViewModel(
        viewModelReference = DadJokeIndexFragment::viewModel,
        defaultState = mockDadJokeIndexState,
        defaultArgs = null
    ) {
        stateForLoadingAndFailure { ::request }
    }
}
