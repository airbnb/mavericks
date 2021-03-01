@file:Suppress("ClassName")

package com.airbnb.mvrx.navigation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.InternalMavericksApi
import java.io.Serializable

private object UninitializedValue

/**
 * This was copied from SynchronizedLazyImpl but modified to automatically initialize in ON_CREATE.
 */
@InternalMavericksApi
@SuppressWarnings("Detekt.ClassNaming")
class navigationLifecycleAwareLazy<out T>(
    owner: LifecycleOwner,
    initializer: () -> T
) : Lazy<T>, Serializable {
    private var initializer: (() -> T)? = initializer

    @Volatile
    @SuppressWarnings("Detekt.VariableNaming")
    private var _value: Any? = UninitializedValue

    // final field is required to enable safe publication of constructed instance
    private val lock = this

    init {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                try {
                    if (!isInitialized()) value
                    owner.lifecycle.removeObserver(this)
                } catch (cause: IllegalStateException) {
                    throw createNavControllerException(cause)
                }
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
                    val typedValue =
                        @Suppress("Detekt.TooGenericExceptionCaught")
                        try {
                            initializer!!()
                        } catch (@SuppressWarnings("Detekt.TooGenericExceptionCaught") cause: Throwable) {
                            throw createNavControllerException(cause)
                        }
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UninitializedValue

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    private fun createNavControllerException(cause: Throwable): IllegalStateException =
        IllegalNavigationStateException(
            """
            NavController is not always accessible before onViewCreated.
            
            During device re-configuration or launch after process death the NavController is not accessible and thus any
            Nav Graph ViewModel is also not accessible. You will need to moving any ViewModel access to onViewCreated or later 
            in the fragment views lifecycle to ensure the ViewModel can be accessed. 
            """.trimIndent(),
            cause
        )
}

class IllegalNavigationStateException(message: String, cause: Throwable) : IllegalStateException(message, cause)
