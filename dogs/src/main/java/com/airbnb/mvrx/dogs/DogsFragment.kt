package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.dogs.app.withModels
import com.airbnb.mvrx.dogs.views.dogRow
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_dogs.adoptButton
import kotlinx.android.synthetic.main.fragment_dogs.dogsRecyclerView
import kotlinx.android.synthetic.main.fragment_dogs.loadingAnimation

class DogsFragment : BaseMvRxFragment() {

    private val viewModel: DogsViewModel by activityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.asyncSubscribe(DogsState::adoptionRequest, onSuccess = {
            findNavController().navigate(R.id.action_dogs_to_adoption)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dogs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adoptButton.setOnClickListener { viewModel.adoptLovedDog() }
    }

    override fun invalidate() = withState(viewModel) { state ->
        loadingAnimation.isVisible = state.dogs is Loading || state.adoptionRequest is Loading

        adoptButton.text = requireContext().getString(R.string.adopt_dog, state.lovedDog?.name)
        adoptButton.isVisible = state.lovedDog != null && state.adoptionRequest !is Loading

        dogsRecyclerView.isVisible = state.adoptionRequest is Uninitialized
        dogsRecyclerView.withModels {
            state.dogs()?.forEach { dog ->
                dogRow {
                    id(dog.id)
                    dog(dog)
                    clickListener { _ -> findNavController().navigate(R.id.action_dogs_to_dogDetail, DogDetailFragment.args(dog.id)) }
                }
            }
        }
    }
}