package com.airbnb.mvrx.hellokoin

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment

fun <A: Fragment> FragmentActivity.findFragmentById(@IdRes id: Int): A {
    @Suppress("UNCHECKED_CAST")
    return supportFragmentManager.findFragmentById(id) as A
}

inline fun <reified T> FragmentActivity.getCurrentFragment(): T {
    val fragment = this.findFragmentById<NavHostFragment>(R.id.rootContainer)
    return fragment.childFragmentManager.fragments.firstOrNull() as T
}