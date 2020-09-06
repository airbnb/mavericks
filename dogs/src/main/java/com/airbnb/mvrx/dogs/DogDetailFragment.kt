package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.dogs.databinding.DogDetailFragmentBinding
import com.airbnb.mvrx.dogs.views.DogDetailFragmentHandler
import com.airbnb.mvrx.withState

class DogDetailFragment : Fragment(), MvRxView, DogDetailFragmentHandler {

    private val viewModel: DogViewModel by activityViewModel()
    private val dogId: Long by args()

    private lateinit var binding: DogDetailFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DogDetailFragmentBinding.inflate(inflater, container, false)
        binding.handler = this
        return binding.root
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
