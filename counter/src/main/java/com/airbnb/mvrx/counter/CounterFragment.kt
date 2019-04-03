package com.airbnb.mvrx.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.Fade
import kotlinx.android.synthetic.main.fragment_counter.counterText
import kotlinx.android.synthetic.main.fragment_counter.nextButton

class CounterFragment : Fragment() {

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_counter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nextButton.setOnClickListener {
            findNavController().navigate(R.id.counterFragment)
        }

        counterText.setOnClickListener {
            count++
            updateCounter()
        }
        updateCounter()
    }

    private fun updateCounter() {
        counterText.text = count.toString()
    }
}
