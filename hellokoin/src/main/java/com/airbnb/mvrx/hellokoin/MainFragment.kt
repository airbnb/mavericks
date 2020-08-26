package com.airbnb.mvrx.hellokoin

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxFragment
import kotlinx.android.synthetic.main.fragment_main.helloButton
import kotlinx.android.synthetic.main.fragment_main.scopedHelloButton

class MainFragment : BaseMvRxFragment(R.layout.fragment_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        helloButton.setOnClickListener { navigateTo(R.id.action_mainFragment_to_helloFragment) }
        scopedHelloButton.setOnClickListener { navigateTo(R.id.action_mainFragment_to_scopedHelloFragment) }
    }

    override fun invalidate() {
        // No-op
    }

    protected fun navigateTo(@IdRes actionId: Int) {
        findNavController().navigate(actionId, null)
    }
}