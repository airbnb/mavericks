package com.airbnb.mvrx.helloDagger.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.mvrx.helloDagger.appComponent
import com.airbnb.mvrx.helloDagger.di.AssistedViewModelFactory
import javax.inject.Inject

/**
 * Base class for Activities.
 *
 * This class contains an injected map of [AssistedViewModelFactory]s to make it easier to create
 * and instantiate requested ViewModels.
 */
abstract class MvRxActivity(@LayoutRes layoutResId: Int = 0) : AppCompatActivity(layoutResId) {

    @Inject
    lateinit var viewModelFactoryMap: @JvmSuppressWildcards Map<Class<out MvRxViewModel<*>>, AssistedViewModelFactory<*, *>>

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)
    }

}