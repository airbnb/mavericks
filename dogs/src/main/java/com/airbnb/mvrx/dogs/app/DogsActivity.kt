package com.airbnb.mvrx.dogs.app

import android.os.Bundle
import com.airbnb.mvrx.BaseMvRxActivity
import com.airbnb.mvrx.dogs.R

class DogsActivity : BaseMvRxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dogs)
    }
}
