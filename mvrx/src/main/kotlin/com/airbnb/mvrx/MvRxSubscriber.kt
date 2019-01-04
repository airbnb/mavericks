package com.airbnb.mvrx

interface MvRxSubscriber<T> {

    fun subscribe(subscriber: (T) -> Unit)
}

internal class DefaultSubscriber<T> : MvRxSubscriber<T> {

    private var subscriber: (T) -> Unit = {}

    override fun subscribe(subscriber: (T) -> Unit) {
        this.subscriber = subscriber
    }

    fun emit(value: T) {
        subscriber(value)
    }
}
