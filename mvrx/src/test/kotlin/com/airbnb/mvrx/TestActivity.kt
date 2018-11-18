package com.airbnb.mvrx

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

open class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}