package com.airbnb.mvrx.sample.anvil.di

interface DaggerComponentOwner {
    /** This is either a component, or a list of components. */
    val daggerComponent: Any
}