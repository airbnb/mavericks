package com.airbnb.mvrx

import android.os.Bundle

/**
 * This interface is implemented by the MvRx PersistState librarty.
 * It has the ability to take a state class and persist any properties annotated with @PersistState to
 * a bundle and then restore those properties in a new state class in the future.
 */
interface StatePersistor {
    fun <S : MvRxState> persistState(obj: S, assertCollectionPersistability: Boolean = false): Bundle

    fun <S : MvRxState> restorePersistedState(bundle: Bundle, initialState: S): S

    /**
     * Kotlin reflection has a large overhead the first time you run it
     * but then is pretty fast on subsequent times. Running these methods now will
     * initialize kotlin reflect and warm the cache so that when persistState() gets
     * called synchronously in onSaveInstanceState() on the main thread, it will be much faster.
     * This improved performance 10-100x for a state with 100 @PersistState properties.
     *
     * This is also @Synchronized to prevent a ConcurrentModificationException in kotlin-reflect: https://gist.github.com/gpeal/27a5747b3c351d4bd592a8d2d58f134a
     */
    fun <S : MvRxState> warmReflectionCache(initialState: S)
}