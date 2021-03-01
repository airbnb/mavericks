@file:Suppress("ClassName")

package com.airbnb.mvrx

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable

private object UninitializedValue

/**
 * This was copied from SynchronizedLazyImpl but modified to automatically initialize in ON_CREATE.
 */
@InternalMavericksApi
@SuppressWarnings("Detekt.ClassNaming")
class lifecycleAwareLazy<out T>(private val owner: LifecycleOwner, initializer: () -> T) :
    Lazy<T>,
    Serializable {
    private var initializer: (() -> T)? = initializer

    @Volatile
    @SuppressWarnings("Detekt.VariableNaming")
    private var _value: Any? = UninitializedValue

    // final field is required to enable safe publication of constructed instance
    private val lock = this

    init {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                if (!isInitialized()) value
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    @Suppress("LocalVariableName")
    override val value: T
        get() {
            @SuppressWarnings("Detekt.VariableNaming")
            val _v1 = _value
            if (_v1 !== UninitializedValue) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                @SuppressWarnings("Detekt.VariableNaming")
                val _v2 = _value
                if (_v2 !== UninitializedValue) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    val typedValue = initializer!!()
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UninitializedValue

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}
