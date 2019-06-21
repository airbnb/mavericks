package com.airbnb.mvrx

import io.reactivex.disposables.Disposable

class Memoize<T>(private val factory: (key: String) -> Pair<T, Disposable>) {
    private var key: String? = null
    private var value: T? = null
    private var disposable: Disposable? = null

    val isInitialized get() = value != null

    @Synchronized
    fun get(key: String): T {
        val currentValue = value
        if (this.key == key && currentValue != null && disposable?.isDisposed != true) return currentValue

        disposable?.dispose()
        this.key = key
        val (value, disposable) = factory(key)
        this.value = value
        this.disposable = disposable
        return value
    }
}