package com.airbnb.mvrx.news.di

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.news.NewsApplication

/**
 * An extension property to make getting hold of the [AppComponent] easier from a Fragment.
 */
val Fragment.appComponent: AppComponent
    get() {
        return (requireActivity().application as NewsApplication).appComponent
    }