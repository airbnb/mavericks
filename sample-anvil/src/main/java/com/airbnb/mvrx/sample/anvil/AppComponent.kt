package com.airbnb.mvrx.anvil

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.sample.anvil.di.AssistedViewModelFactory
import com.airbnb.mvrx.sample.anvil.UserComponent
import com.airbnb.mvrx.sample.anvil.di.SingleIn
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent

interface AppScope

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface AppComponent {
    fun userComponentBuilder(): UserComponent.Builder
}

// TODO: can this be moved into the component?
@ContributesTo(AppScope::class)
interface AppScopeBindings {
    fun viewModelFactories(): Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
}
