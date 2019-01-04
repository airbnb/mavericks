package com.airbnb.mvrx

/**
 * Interface that has to be implemented by all Kotlin data classes that will be used as state.
 *
 * This is currently only used to configure Proguard to correctly work with MvRx.
 *
 * If you need to use Fragment arguments to initialize your state, create a secondary constructor
 * that takes a parcelable object. If the Fragment that initializes this ViewModel has an argument
 * at [MvRx.KEY_ARG], it will be passed to your secondary constructor automatically.
 */
interface MvRxState {

    operator fun <A> A.unaryPlus() = tuple(this)
}
