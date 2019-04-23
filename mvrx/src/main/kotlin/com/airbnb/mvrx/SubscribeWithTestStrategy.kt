package com.airbnb.mvrx

import io.reactivex.observers.DisposableObserver

internal class SubscribeWithTestStrategy : SubscribeWithStrategy {
    override fun <T : Any> subscribe(subscriber: (T) -> Unit): ObserverDisposable<T> {
        return object : ObserverDisposable<T>, DisposableObserver<T>() {
            override fun onComplete() {}
            override fun onNext(value: T) {
                subscriber(value)
            }

            override fun onError(e: Throwable) {}
        }
    }
}