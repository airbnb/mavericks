package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

data class ViewModelTestState(val foo: Int = 0, val bar: Int = 0, val bam: Int = 0) : MvRxState
class ViewModelTestViewModel(initialState: ViewModelTestState) : TestMvRxViewModel<ViewModelTestState>(initialState) {

    var subscribeCallCount = 0
    var subscribeWithHistoryCallCount = 0
    var selectSubscribe1Called = 0
    var selectSubscribe2Called = 0
    var selectSubscribe3Called = 0

    init {
        subscribe { _ -> subscribeCallCount++ }
        subscribeWithHistory { _, _ ->  subscribeWithHistoryCallCount++ }
        selectSubscribe(ViewModelTestState::foo) { selectSubscribe1Called++ }
        selectSubscribe(ViewModelTestState::foo, ViewModelTestState::bar) { _, _ -> selectSubscribe2Called++ }
        selectSubscribe(ViewModelTestState::foo, ViewModelTestState::bar, ViewModelTestState::bam) { _, _, _ -> selectSubscribe3Called++ }
    }

    fun setFoo(foo: Int) = setState { copy(foo = foo) }

    fun setBar(bar: Int) = setState { copy(bar = bar) }

    fun setBam(bam: Int) = setState { copy(bam = bam) }

    fun disposeOnClear(disposable: Disposable) {
        disposable.disposeOnClear()
    }

    fun triggerCleared() {
        onCleared()
    }
}

class ViewModelSubscriberTest : BaseTest() {

    private lateinit var viewModel: ViewModelTestViewModel
    private lateinit var owner: TestLifecycleOwner

    @Before
    fun setup() {
        viewModel = ViewModelTestViewModel(ViewModelTestState())
        owner = TestLifecycleOwner()
        owner.lifecycle.markState(Lifecycle.State.RESUMED)
    }

    @Test
    fun testSubscribeWithHistory() {
        assertEquals(1, viewModel.subscribeWithHistoryCallCount)
    }

    @Test
    fun testSubscribeWithHistoryExternal() {
        var callCount = 0
        viewModel.subscribeWithHistory(owner) { oldState, _ ->
            callCount++
            assertNull(oldState)
        }
        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribe() {
        assertEquals(1, viewModel.subscribeCallCount)
    }

    @Test
    fun testSubscribeExternal() {
        var callCount = 0
        viewModel.subscribe(owner) { callCount++ }
        assertEquals(1, callCount)
    }

    @Test
    fun testSelectSubscribe() {
        assertEquals(1, viewModel.selectSubscribe1Called)
    }

    @Test
    fun testSelectSubscribe1External() {
        var callCount = 0
        viewModel.selectSubscribe(owner, ViewModelTestState::foo) { callCount++ }
        assertEquals(1, callCount)
        viewModel.setFoo(1)
        assertEquals(2, callCount)
    }

    @Test
    fun testNotChangingFoo() {
        viewModel.setFoo(0)
        assertEquals(1, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(1, viewModel.selectSubscribe2Called)
        assertEquals(1, viewModel.selectSubscribe3Called)
        assertEquals(1, viewModel.subscribeWithHistoryCallCount)
    }

    @Test
    fun testChangingFoo() {
        viewModel.setFoo(1)
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(2, viewModel.selectSubscribe1Called)
        assertEquals(2, viewModel.selectSubscribe2Called)
        assertEquals(2, viewModel.selectSubscribe3Called)
        assertEquals(2, viewModel.subscribeWithHistoryCallCount)
    }

    @Test
    fun testChangingBar() {
        viewModel.setBar(1)
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(2, viewModel.selectSubscribe2Called)
        assertEquals(2, viewModel.selectSubscribe3Called)
        assertEquals(2, viewModel.subscribeWithHistoryCallCount)
    }

    @Test
    fun testChangingBam() {
        viewModel.setBam(1)
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(1, viewModel.selectSubscribe2Called)
        assertEquals(2, viewModel.selectSubscribe3Called)
        assertEquals(2, viewModel.subscribeWithHistoryCallCount)
    }

    @Test
    fun testDisposeOnClear() {
        val disposable = Maybe.never<Int>().subscribe()
        viewModel.disposeOnClear(disposable)
        assertFalse(disposable.isDisposed)
        viewModel.triggerCleared()
        assertTrue(disposable.isDisposed)
    }
}