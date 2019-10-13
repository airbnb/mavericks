package com.airbnb.mvrx.helloDagger

import android.os.Bundle
import com.airbnb.mvrx.helloDagger.base.MvRxActivity

class MainActivity : MvRxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.rootContainer, HelloFragment())
                    .commit()
        }
    }
}
