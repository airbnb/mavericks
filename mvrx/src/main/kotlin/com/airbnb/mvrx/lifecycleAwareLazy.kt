@file:Suppress("ClassName")

package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable
import java.io.Serializable

/**
 * This was copied from SynchronizedLazyImpl but modified to automatically initialize in ON_CREATE.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("Detekt.ClassNaming")
class lifecycleAwareLazy<out T>(private val owner: LifecycleOwner, private val keyFactory: () -> String, initializer: (key: String) -> Pair<T, Disposable>) : Lazy<T>,
    Serializable {
    @SuppressWarnings("Detekt.VariableNaming")
    private val _value = Memoize<T>(initializer)
    // final field is required to enable safe publication of constructed instance
    private val lock = this

    init {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                value
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    @Suppress("LocalVariableName")
    override val value: T
        get() = synchronized(lock) {
            _value.get(keyFactory())
        }

    override fun isInitialized() = _value.isInitialized
}