package com.airbnb.mvrx.hellodagger

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.hellodagger.di.AssistedViewModelFactory
import com.airbnb.mvrx.hellodagger.di.SingleIn
import com.gpeal.droidconanvilsample.lib.daggerscopes.AppScope
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.MergeSubcomponent

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface AppComponent

@ContributesTo(AppScope::class)
interface AppScopeBindings {
    fun viewModelFactories(): Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
}