package com.airbnb.mvrx

import android.os.Bundle

open class TestMvRxActivity : MvRxActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}