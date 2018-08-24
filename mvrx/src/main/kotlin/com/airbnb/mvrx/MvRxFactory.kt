package com.airbnb.mvrx

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

internal class MvRxFactory<V>(private val factory: (Class<*>) -> V) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel?> create(modelClass: Class<T>) = factory(modelClass) as T
}