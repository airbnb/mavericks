package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.dogs.databinding.AdoptionFragmentBinding
import com.airbnb.mvrx.withState

class AdoptionFragment : BaseMvRxFragment() {
    private val viewModel: DogViewModel by activityViewModel()
    private lateinit var binding: AdoptionFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AdoptionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.dog = state.adoptionRequest()
    }
}