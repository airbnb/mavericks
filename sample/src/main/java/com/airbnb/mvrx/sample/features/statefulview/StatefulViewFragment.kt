package com.airbnb.mvrx.sample.features.statefulview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.R

class StatefulViewFragment : BaseMvRxFragment() {
    private val fragmentScopedViewModel: CounterViewModel by fragmentViewModel()
    private val activityScopedViewModel: CounterViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_stateful_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Toolbar>(R.id.toolbar).setupWithNavController(findNavController())
        view.findViewById<View>(R.id.navigate).setOnClickListener {
            findNavController().navigate(R.id.action_statefulViewFragment_self)
        }

        view.findViewById<View>(R.id.increment_fragment).setOnClickListener {
            fragmentScopedViewModel.incrementCount()
        }

        view.findViewById<View>(R.id.increment_activity).setOnClickListener {
            activityScopedViewModel.incrementCount()
        }

        val toggledView = view.findViewById<View>(R.id.attach_count)
        val toggledViewParent = toggledView.parent as ViewGroup
        view.findViewById<View>(R.id.toggle_view).setOnClickListener {
            if (toggledView.isAttachedToWindow) {
                toggledViewParent.removeView(toggledView)
            } else {
                toggledViewParent.addView(toggledView)
            }
        }
    }

    override fun invalidate() {
        // Do nothing.
    }
}