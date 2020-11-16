package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.dogs.data.Dog
import com.airbnb.mvrx.dogs.databinding.DogsFragmentBinding
import com.airbnb.mvrx.dogs.views.DogsFragmentHandler
import com.airbnb.mvrx.withState

class DogsFragment : Fragment(), MvRxView, DogsFragmentHandler {

    private val viewModel: DogViewModel by activityViewModel()
    private lateinit var bindings: DogsFragmentBinding
    private val adapter = DogAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bindings = DogsFragmentBinding.inflate(inflater, container, false)
        bindings.dogsRecyclerView.adapter = adapter
        bindings.handler = this
        return bindings.root
    }

    override fun onDogClicked(dog: Dog) {
        findNavController().navigate(R.id.action_dogs_to_dogDetail, DogDetailFragment.arg(dog.id))
    }

    override fun adoptLovedDog() {
        viewModel.adoptLovedDog()
        findNavController().navigate(R.id.action_dogs_to_adoption)
    }

    override fun invalidate() = withState(viewModel) { state ->
        bindings.state = state
    }
}
