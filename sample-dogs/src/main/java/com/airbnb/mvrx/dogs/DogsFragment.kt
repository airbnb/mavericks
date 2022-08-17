package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.dogs.data.Dog
import com.airbnb.mvrx.dogs.databinding.DogsFragmentBinding
import com.airbnb.mvrx.dogs.views.DogsFragmentHandler
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

class DogsFragment : Fragment(R.layout.dogs_fragment), MavericksView, DogsFragmentHandler {
    private val binding: DogsFragmentBinding by viewBinding()
    private val viewModel: DogViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.handler = this
        binding.dogsRecyclerView.adapter = DogAdapter(this)
    }

    override fun onDogClicked(dog: Dog) {
        findNavController().navigate(R.id.action_dogs_to_dogDetail, DogDetailFragment.arg(dog.id))
    }

    override fun adoptLovedDog() {
        viewModel.adoptLovedDog()
        findNavController().navigate(R.id.action_dogs_to_adoption)
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.state = state
    }
}
