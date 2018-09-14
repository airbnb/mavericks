package com.airbnb.mvrx

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * A [MvRxStateStore] which ignores standard calls to [set]. Instead it can be scripted via calls to
 * [next]. This is intended to be used to test how
 */
class ScriptableMvRxStateStore<S : Any>(initialState: S) : MvRxStateStore<S> {

    private val subject: BehaviorSubject<S> = BehaviorSubject.createDefault(initialState)

    override val observable: Observable<S> = subject.distinctUntilChanged()

    override val state: S
        get() = subject.value!!

    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        // No-op set the state via next
    }

    fun next(state: S) = subject.onNext(state)

    override fun isDisposed() = false

    override fun dispose() {}
}