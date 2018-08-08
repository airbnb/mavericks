package com.airbnb.mvrx.sample

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.sample.core.BaseMvRxFragment
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.marquee

class MainFragment : BaseMvRxFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.visibility = View.GONE
    }

    override fun EpoxyController.buildModels() {
        marquee {
            id("marquee")
            title("Welcome to MvRx")
            subtitle("Select a demo below")
        }

        basicRow {
            id("dad_jokes")
            title("Pagination (Dad Jokes)")
            clickListener { _ -> findNavController().navigate(R.id.dadJokes) }
        }
    }
}