package com.airbnb.mvrx.counter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_counter.text_view

class CounterFragment : Fragment(R.layout.fragment_counter) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        text_view.setOnClickListener {
            TODO()
        }
    }
}
