package com.airbnb.mvrx.dogs

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.dogs.databinding.AdoptionFragmentBinding
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

class AdoptionFragment : Fragment(R.layout.adoption_fragment), MavericksView {
    private val binding: AdoptionFragmentBinding by viewBinding()
    private val viewModel: DogViewModel by activityViewModel()

    override fun invalidate() = withState(viewModel) { state ->
        binding.dog = state.adoptionRequest()
    }
}
