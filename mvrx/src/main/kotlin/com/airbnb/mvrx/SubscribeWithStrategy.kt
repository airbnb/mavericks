package com.airbnb.mvrx

internal interface SubscribeWithStrategy {
    fun <T : Any> subscribe(subscriber: (T) -> Unit): ObserverDisposable<T>
}