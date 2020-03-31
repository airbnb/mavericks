@file:Suppress("ClassName")

package com.airbnb.mvrx.navigation

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable

private object UninitializedValue

/**
 * This forked from com.airbnb.mvrx.lifecycleAwareLazy
 *
 * Instead of ON_CREATE from lifecycleAwareLazy, navigationLifecycleAwareLazy is automatically initialize in ON_START
 *
 * This fork is required due to how NavHost lifecycle works during re-configuration. NavHost is not
 * accessible in onCreate during re-creation life on configuration change, instead. navigation ViewModels
 * should always be accessed on or after onViewCreated as this is when the NavHost is guaranteed to exist.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
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

    @VisibleForTesting
    internal val lifecycleObserver: DefaultLifecycleObserver =
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    try {
                        if (!isInitialized()) value
                        owner.lifecycle.removeObserver(this)
                    } catch (cause: IllegalStateException) {
                        throw IllegalStateException(
                                """
                            During device re-configuration or launch after process death the navController is not accessible until onViewCreated.
                             Try moving any ViewModel access to onViewCreated or later and use subscriptionLifecycleOwner for any subscriptions.
                        """.trimIndent(),
                                cause
                        )
                    }
                }
            }

    init {
        owner.lifecycle.addObserver(lifecycleObserver)
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
