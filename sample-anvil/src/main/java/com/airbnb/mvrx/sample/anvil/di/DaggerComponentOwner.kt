package com.airbnb.mvrx.sample.anvil.di

/**
 * A [DaggerComponentOwner] is anything that "owns" a Dagger Component. The owner can be a Application, Activity, or Fragment.
 *
 * When using [bindings] or when creating a ViewModel, the infrastructure provided will walk up the Fragment tree, then check
 * the Activity, then the Application for any [DaggerComponentOwner] that can provide ViewModels.
 *
 * In this sample:
 * * [com.airbnb.mvrx.sample.anvil.AppComponent] is owned by the Application class.
 * * [com.airbnb.mvrx.sample.anvil.UserComponent] is subcomponent of AppComponent and injected with the current logged in user.
 *   It is set/cleared in the Application class.
 * * [com.airbnb.mvrx.sample.anvil.ExampleFeatureComponent] is a subcomponent of UserComponent and
 *   owned by [com.airbnb.mvrx.sample.anvil.ExampleFeatureFragment].
 */
interface DaggerComponentOwner {
    /** This is either a component, or a list of components. */
    val daggerComponent: Any
}
