package com.airbnb.mvrx.sample.core

import kotlin.reflect.KProperty

// Like Kotlin's lazy delegate but the initializer gets the target and metadata passed to it
class ViewDelegate<in T, out V>(private val initializer: (T, KProperty<*>) -> V) {
    private object EMPTY

    private var view: Any? = EMPTY

    operator fun getValue(thisRef: T, property: KProperty<*>): V {
        if (view === EMPTY) {
            view = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return view as V
    }

    fun clear() {
        view = EMPTY
    }
}