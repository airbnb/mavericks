package com.airbnb.mvrx.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.mvrx.navigation.test.R

class HostFragment : Fragment(R.layout.mvrx_fragment_host) {

    var viewModel: NavigationViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment: Fragment = NavHostFragment.create(R.navigation.test_graph)
        childFragmentManager.commit {
            replace(R.id.container, navHostFragment)
        }
    }
}
