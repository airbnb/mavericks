package com.airbnb.mvrx.helloDagger

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.helloDagger.base.MvRxActivity

// Useful for getting access to a fragment in tests
fun <A: Fragment> MvRxActivity.findFragmentById(@IdRes id: Int): A {
    @Suppress("UNCHECKED_CAST")
    return supportFragmentManager.findFragmentById(id) as A
}