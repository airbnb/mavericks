package com.airbnb.mvrx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class TestMvRxActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}
