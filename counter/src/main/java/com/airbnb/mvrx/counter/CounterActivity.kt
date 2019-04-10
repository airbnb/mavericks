package com.airbnb.mvrx.counter

import android.os.Bundle
import com.airbnb.mvrx.BaseMvRxActivity

class CounterActivity : BaseMvRxActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)
    }
}
