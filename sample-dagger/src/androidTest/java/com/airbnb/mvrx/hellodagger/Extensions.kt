package com.airbnb.mvrx.hellodagger

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

// Useful for getting access to a fragment in tests
fun <A : Fragment> FragmentActivity.findFragmentById(@IdRes id: Int): A {
    @Suppress("UNCHECKED_CAST")
    return supportFragmentManager.findFragmentById(id) as A
}