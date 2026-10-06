package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.dogs.databinding.DogDetailFragmentBinding
import com.airbnb.mvrx.dogs.views.DogDetailFragmentHandler
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

class DogDetailFragment : Fragment(R.layout.dog_detail_fragment), MavericksView, DogDetailFragmentHandler {
    private val binding: DogDetailFragmentBinding by viewBinding()
    private val viewModel: DogDetailViewModel by fragmentViewModel()
    private val dogViewModel: DogViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.handler = this
    }

    override fun onLoveClicked() {
        withState(viewModel) {
            dogViewModel.loveDog(it.dogId)
        }
        findNavController().popBackStack()
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (state.dog) {
            is Fail -> {
                binding.progressBar.hide()
                binding.error.isVisible = true
                binding.error.text = state.dog.error.localizedMessage
            }
            is Loading, Uninitialized -> {
                binding.progressBar.show()
                binding.error.isVisible = false
            }
            is Success -> {
                binding.progressBar.hide()
                binding.error.isVisible = false
                binding.dog = state.dog()
            }
        }
    }

    companion object {
        const val ARG_DOG_ID = "dogId"
        fun arg(dogId: Long) = bundleOf(ARG_DOG_ID to dogId)
    }
}
