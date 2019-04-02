package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_dogs.dogsRecyclerView

class DogsFragment : BaseMvRxFragment() {

    private val viewModel: DogsViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dogs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dogsRecyclerView.setItemSpacingDp(24)
        dogsRecyclerView.buildModelsWith { it.buildModels() }
    }

    override fun invalidate() = withState(viewModel) { _ ->
        dogsRecyclerView.requestModelBuild()
    }

    private fun EpoxyController.buildModels() = withState(viewModel) { state ->
        state.dogs()?.forEach { dog ->
            dogRow {
                id(dog.id)
                dog(dog)
                clickListener { _ -> findNavController().navigate(R.id.action_dogs_to_dogDetail, DogDetailFragment.args(dog.id)) }
            }
        }
    }
}