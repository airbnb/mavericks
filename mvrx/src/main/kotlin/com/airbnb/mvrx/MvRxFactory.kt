package com.airbnb.mvrx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
class MvRxFactory<V>(private val factory: (Class<*>) -> V) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel?> create(modelClass: Class<T>) = factory(modelClass) as T
}