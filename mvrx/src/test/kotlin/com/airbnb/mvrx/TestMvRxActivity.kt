package com.airbnb.mvrx

import android.os.Bundle

open class TestMvRxActivity : BaseMvRxActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}