package com.airbnb.mvrx.sample.di

import android.arch.lifecycle.ViewModel
import com.airbnb.mvrx.sample.features.dadjoke.DadJokeDetailViewModel
import com.airbnb.mvrx.sample.features.dadjoke.DadJokeIndexViewModel
import com.airbnb.mvrx.sample.features.dadjoke.RandomDadJokeViewModel
import com.airbnb.mvrx.sample.features.flow.FlowViewModel
import com.airbnb.mvrx.sample.features.helloworld.HelloWorldViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Provide all ViewModels to [ViewModelFactory]
 */
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(HelloWorldViewModel::class)
    abstract fun bindViewModel1(viewModel: HelloWorldViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FlowViewModel::class)
    abstract fun bindViewModel2(viewModel: FlowViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DadJokeIndexViewModel::class)
    abstract fun bindViewModel3(viewModel: DadJokeIndexViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DadJokeDetailViewModel::class)
    abstract fun bindViewModel4(viewModel: DadJokeDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RandomDadJokeViewModel::class)
    abstract fun bindViewModel5(viewModel: RandomDadJokeViewModel): ViewModel
}