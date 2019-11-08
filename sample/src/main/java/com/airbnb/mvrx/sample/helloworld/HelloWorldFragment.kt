package com.airbnb.mvrx.sample.helloworld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.views.Marquee

class HelloWorldFragment : BaseMvRxFragment() {
    private lateinit var marquee: Marquee

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_hello_world, container, false).apply {
            marquee = findViewById(R.id.marquee)
        }

    override fun invalidate() {
        marquee.setTitle("Hello World")
    }
}