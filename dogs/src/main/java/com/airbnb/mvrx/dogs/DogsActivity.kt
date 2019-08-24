package com.airbnb.mvrx.dogs

import android.os.Bundle
import com.airbnb.mvrx.BaseMvRxActivity

class DogsActivity : BaseMvRxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dogs_activity)
    }
}
