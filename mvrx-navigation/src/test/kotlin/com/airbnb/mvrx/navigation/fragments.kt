package com.airbnb.mvrx.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.navigation.test.R

abstract class BaseFirstTestNavigationFragment : Fragment(R.layout.mvrx_text), MavericksView {

    protected val viewModel: NavigationViewModel by navGraphViewModel(R.id.storeFragment)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var fragment = parentFragment
        while (fragment != null && fragment !is HostFragment) {
            fragment = fragment.parentFragment
        }

        val hostFragment = fragment as HostFragment
        if (hostFragment.viewModel == null) {
            hostFragment.viewModel = viewModel
        }
    }

    override fun invalidate() {}
}

class FirstTestNavigationFragment : BaseFirstTestNavigationFragment() {

    companion object {
        const val TEST_VALUE = "producer"
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateProducer(TEST_VALUE)
    }
}

class SecondTestNavigationFragment : BaseFirstTestNavigationFragment() {

    companion object {
        const val TEST_VALUE = "consumer"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (HostFragment.accessViewModelInOnCreate) {
            viewModel.updateConsumer("create-consumer")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateConsumer(TEST_VALUE)
    }
}
