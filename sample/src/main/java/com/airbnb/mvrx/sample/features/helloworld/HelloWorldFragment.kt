package com.airbnb.mvrx.sample.features.helloworld

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.views.Marquee

class HelloWorldFragment : BaseMvRxFragment() {
    private lateinit var marquee: Marquee

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_hello_world, container, false).apply {
            findViewById<Toolbar>(R.id.toolbar).setupWithNavController(findNavController())
            marquee = findViewById(R.id.marquee)
        }

    override fun invalidate() {
        marquee.setTitle("Hello World")
    }
}