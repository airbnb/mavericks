package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.dogs.databinding.DogDetailFragmentBinding
import com.airbnb.mvrx.dogs.views.DogDetailFragmentHandler
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

class DogDetailFragment : Fragment(R.layout.dog_detail_fragment), MavericksView, DogDetailFragmentHandler {
    private val binding: DogDetailFragmentBinding by viewBinding()
    private val viewModel: DogViewModel by activityViewModel()
    private val dogId: Long by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.handler = this
    }

    override fun onLoveClicked() {
        viewModel.loveDog(dogId)
        findNavController().popBackStack()
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.dog = state.dog(dogId) ?: error("Cannot find dog with id $dogId")
    }

    companion object {
        fun arg(dogId: Long) = bundleOf(Mavericks.KEY_ARG to dogId)
    }
}
