package com.airbnb.mvrx.mock

import com.airbnb.mvrx.MvRxStateStore
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * This acts as a functional state store, but all updates happen synchronously.
 * The intention of this is to allow state changes in tests to be tracked
 * synchronously.
 */
internal class ImmediateRealMvRxStateStore<S : Any>(initialState: S) : MvRxStateStore<S> {
    private var disposed = false

    private val subject: BehaviorSubject<S> = BehaviorSubject.createDefault(initialState)

    // Using "trampoline" scheduler so that updates are processed synchronously
    override val observable: Observable<S> = subject.toSerialized().observeOn(Schedulers.trampoline()).distinctUntilChanged()

    override val state: S
        get() = subject.value!!

    override fun dispose() {
        disposed = true
    }

    override fun isDisposed(): Boolean = disposed

    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        subject.onNext(stateReducer(state))
    }
}
