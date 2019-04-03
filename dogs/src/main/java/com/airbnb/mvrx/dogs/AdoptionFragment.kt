package com.airbnb.mvrx.dogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.dogs.views.dogRow
import com.airbnb.mvrx.dogs.views.titleRow
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_adoption.dogsRecyclerView

class AdoptionFragment : BaseMvRxFragment() {
    private val viewModel: DogsViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_adoption, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dogsRecyclerView.buildModelsWith { it.buildModels() }
    }

    override fun invalidate() {
        dogsRecyclerView.requestModelBuild()
    }

    private fun EpoxyController.buildModels() = withState(viewModel) { state ->
        titleRow {
            id("title")
            title(R.string.meet_your_dog)
        }
        state.adoptionRequest()?.let { dog ->
            dogRow {
                id(dog.id)
                dog(dog)
            }
        }
    }
}