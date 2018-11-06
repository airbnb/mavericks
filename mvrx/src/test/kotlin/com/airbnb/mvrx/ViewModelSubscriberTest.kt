package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

data class ViewModelTestState(
        val foo: Int = 0,
        val bar: Int = 0,
        val bam: Int = 0,
        val list: List<Int> = emptyList(),
        val async: Async<String> = Uninitialized,
        val prop6: Int = 0,
        val prop7: Int = 0
) : MvRxState

class ViewModelTestViewModel(initialState: ViewModelTestState) : TestMvRxViewModel<ViewModelTestState>(initialState) {

    var subscribeCallCount = 0
    var selectSubscribe1Called = 0
    var selectSubscribe2Called = 0
    var selectSubscribe3Called = 0
    var onSuccessCalled = 0
    var onFailCalled = 0

    init {
        subscribe { _ -> subscribeCallCount++ }
        selectSubscribe(ViewModelTestState::foo) { selectSubscribe1Called++ }
        selectSubscribe(ViewModelTestState::foo, ViewModelTestState::bar) { _, _ -> selectSubscribe2Called++ }
        selectSubscribe(ViewModelTestState::foo, ViewModelTestState::bar, ViewModelTestState::bam) { _, _, _ -> selectSubscribe3Called++ }
        asyncSubscribe(ViewModelTestState::async, { onFailCalled++ }) { onSuccessCalled++ }
    }

    fun setFoo(foo: Int) = setState { copy(foo = foo) }

    fun setBar(bar: Int) = setState { copy(bar = bar) }

    fun setBam(bam: Int) = setState { copy(bam = bam) }

    fun set(reducer: ViewModelTestState.() -> ViewModelTestState) {
        setState(reducer)
    }

    fun setAsync(async: Async<String>) {
        setState { copy(async = async) }
    }

    fun disposeOnClear(disposable: Disposable) {
        disposable.disposeOnClear()
    }

    fun triggerCleared() {
        onCleared()
    }

    fun testSingleSuccess() {
        var callCount = 0
        selectSubscribe(ViewModelTestState::async) {
            callCount++
            assertEquals(when (callCount) {
                1 -> Uninitialized
                2 -> Loading()
                3 -> Success("Hello World")
                else -> throw IllegalArgumentException("Unexpected call count $callCount")
            }, it)
        }
        Single.create<String> { emitter ->
            emitter.onSuccess("Hello World")
        }.execute { copy(async = it) }
        assertEquals(3, callCount)
    }

    fun testSingleFail() {
        var callCount = 0
        val error = IllegalStateException("Fail")
        selectSubscribe(ViewModelTestState::async) {
            callCount++
            assertEquals(when (callCount) {
                1 -> Uninitialized
                2 -> Loading()
                3 -> Fail<String>(error)
                else -> throw IllegalArgumentException("Unexpected call count $callCount")
            }, it)
        }
        Single.create<String> {
            throw error
        }.execute { copy(async = it) }
        assertEquals(3, callCount)
    }

    fun testObservableSuccess() {
        var callCount = 0
        selectSubscribe(ViewModelTestState::async) {
            callCount++
            assertEquals(when (callCount) {
                1 -> Uninitialized
                2 -> Loading()
                3 -> Success("Hello World")
                else -> throw IllegalArgumentException("Unexpected call count $callCount")
            }, it)
        }
        Observable.just("Hello World").execute { copy(async = it) }
        assertEquals(3, callCount)
    }

    fun testSequentialSetStatesWithinWithStateBlock() {
        var callCount = 0
        withState {
            selectSubscribe(ViewModelTestState::foo) {
                callCount++
                assertEquals(when (callCount) {
                    1 -> 0
                    2 -> 2
                    else -> throw IllegalArgumentException("Unexpected call count $callCount")
                }, it)
            }
            // These will be coalesced into one update
            setState { copy(foo = 1) }
            setState { copy(foo = 2) }
        }
        assertEquals(2, callCount)
    }

    fun testObservableFail() {
        var callCount = 0
        val error = IllegalStateException("Fail")
        selectSubscribe(ViewModelTestState::async) {
            callCount++
            assertEquals(when (callCount) {
                1 -> Uninitialized
                2 -> Loading()
                3 -> Fail<String>(error)
                else -> throw IllegalArgumentException("Unexpected call count $callCount")
            }, it)
        }
        Observable.create<String> {
            throw error
        }.execute { copy(async = it) }
        assertEquals(3, callCount)
    }

    fun testObservableWithMapper() {
        var callCount = 0
        selectSubscribe(ViewModelTestState::async) {
            callCount++
            assertEquals(when (callCount) {
                1 -> Uninitialized
                2 -> Loading()
                3 -> Success("Hello World!")
                else -> throw IllegalArgumentException("Unexpected call count $callCount")
            }, it)
        }
        Observable.just("Hello World").execute(mapper = { "$it!" }) { copy(async = it) }
        assertEquals(3, callCount)

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
    fun testSelectSubscribe2External() {
        var callCount = 0
        viewModel.selectSubscribe(owner, ViewModelTestState::foo, ViewModelTestState::bar) { _, _ -> callCount++ }
        assertEquals(1, callCount)
        viewModel.setFoo(1)
        assertEquals(2, callCount)
        viewModel.setBar(2)
        assertEquals(3, callCount)
    }

    @Test
    fun testSelectSubscribe3External() {
        var callCount = 0
        viewModel.selectSubscribe(
                owner,
                ViewModelTestState::foo,
                ViewModelTestState::bar,
                ViewModelTestState::bam
        ) { _, _, _ -> callCount++ }
        assertEquals(1, callCount)
        viewModel.setFoo(1)
        assertEquals(2, callCount)
        viewModel.setBar(2)
        assertEquals(3, callCount)
        viewModel.setBam(2)
        assertEquals(4, callCount)
    }

    @Test
    fun testSelectSubscribe4External() {
        var callCount = 0
        viewModel.selectSubscribe(
                owner,
                ViewModelTestState::foo,
                ViewModelTestState::bar,
                ViewModelTestState::bam,
                ViewModelTestState::list
        ) { _, _, _, _ -> callCount++ }
        assertEquals(1, callCount)
        viewModel.setFoo(1)
        assertEquals(2, callCount)
        viewModel.setBar(2)
        assertEquals(3, callCount)
        viewModel.setBam(2)
        assertEquals(4, callCount)
        viewModel.set { copy(list = listOf(1, 2, 3)) }
        assertEquals(5, callCount)
    }


    @Test
    fun testSelectSubscribe5External() {
        var callCount = 0
        viewModel.selectSubscribe(
                owner,
                ViewModelTestState::foo,
                ViewModelTestState::bar,
                ViewModelTestState::bam,
                ViewModelTestState::list,
                ViewModelTestState::async
        ) { _, _, _, _, _ -> callCount++ }
        assertEquals(1, callCount)
        viewModel.setFoo(1)
        assertEquals(2, callCount)
        viewModel.setBar(2)
        assertEquals(3, callCount)
        viewModel.setBam(2)
        assertEquals(4, callCount)
        viewModel.set { copy(list = listOf(1, 2, 3)) }
        assertEquals(5, callCount)
        viewModel.set { copy(async = Loading()) }
        assertEquals(6, callCount)
    }

    @Test
    fun testSelectSubscribe6External() {
        var callCount = 0
        viewModel.selectSubscribe(
                owner,
                ViewModelTestState::foo,
                ViewModelTestState::bar,
                ViewModelTestState::bam,
                ViewModelTestState::list,
                ViewModelTestState::async,
                ViewModelTestState::prop6
        ) { _, _, _, _, _, _ -> callCount++ }
        assertEquals(1, callCount)
        viewModel.setFoo(1)
        assertEquals(2, callCount)
        viewModel.setBar(2)
        assertEquals(3, callCount)
        viewModel.setBam(2)
        assertEquals(4, callCount)
        viewModel.set { copy(list = listOf(1, 2, 3)) }
        assertEquals(5, callCount)
        viewModel.set { copy(async = Loading()) }
        assertEquals(6, callCount)
        viewModel.set { copy(prop6 = 1) }
        assertEquals(7, callCount)
    }

    @Test
    fun testSelectSubscribe7External() {
        var callCount = 0
        viewModel.selectSubscribe(
                owner,
                ViewModelTestState::foo,
                ViewModelTestState::bar,
                ViewModelTestState::bam,
                ViewModelTestState::list,
                ViewModelTestState::async,
                ViewModelTestState::prop6,
                ViewModelTestState::prop7
        ) { _, _, _, _, _, _, _ -> callCount++ }
        assertEquals(1, callCount)
        viewModel.setFoo(1)
        assertEquals(2, callCount)
        viewModel.setBar(2)
        assertEquals(3, callCount)
        viewModel.setBam(2)
        assertEquals(4, callCount)
        viewModel.set { copy(list = listOf(1, 2, 3)) }
        assertEquals(5, callCount)
        viewModel.set { copy(async = Loading()) }
        assertEquals(6, callCount)
        viewModel.set { copy(prop6 = 1) }
        assertEquals(7, callCount)
        viewModel.set { copy(prop7 = 1) }
        assertEquals(8, callCount)
    }

    @Test
    fun testNotChangingFoo() {
        viewModel.setFoo(0)
        assertEquals(1, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(1, viewModel.selectSubscribe2Called)
        assertEquals(1, viewModel.selectSubscribe3Called)
        assertEquals(0, viewModel.onSuccessCalled)
        assertEquals(0, viewModel.onFailCalled)
    }

    @Test
    fun testChangingFoo() {
        viewModel.setFoo(1)
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(2, viewModel.selectSubscribe1Called)
        assertEquals(2, viewModel.selectSubscribe2Called)
        assertEquals(2, viewModel.selectSubscribe3Called)
        assertEquals(0, viewModel.onSuccessCalled)
        assertEquals(0, viewModel.onFailCalled)
    }

    @Test
    fun testChangingBar() {
        viewModel.setBar(1)
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(2, viewModel.selectSubscribe2Called)
        assertEquals(2, viewModel.selectSubscribe3Called)
        assertEquals(0, viewModel.onSuccessCalled)
        assertEquals(0, viewModel.onFailCalled)
    }

    @Test
    fun testChangingBam() {
        viewModel.setBam(1)
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(1, viewModel.selectSubscribe2Called)
        assertEquals(2, viewModel.selectSubscribe3Called)
        assertEquals(0, viewModel.onSuccessCalled)
        assertEquals(0, viewModel.onFailCalled)
    }

    @Test
    fun testSuccess() {
        viewModel.setAsync(Success("Hello World"))
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(1, viewModel.selectSubscribe2Called)
        assertEquals(1, viewModel.selectSubscribe3Called)
        assertEquals(1, viewModel.onSuccessCalled)
        assertEquals(0, viewModel.onFailCalled)
    }

    @Test
    fun testFail() {
        viewModel.setAsync(Fail(IllegalStateException("foo")))
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribe1Called)
        assertEquals(1, viewModel.selectSubscribe2Called)
        assertEquals(1, viewModel.selectSubscribe3Called)
        assertEquals(0, viewModel.onSuccessCalled)
        assertEquals(1, viewModel.onFailCalled)
    }

    @Test
    fun testDisposeOnClear() {
        val disposable = Maybe.never<Int>().subscribe()
        viewModel.disposeOnClear(disposable)
        assertFalse(disposable.isDisposed)
        viewModel.triggerCleared()
        assertTrue(disposable.isDisposed)
    }

    @Test
    fun testSingleSuccess() {
        viewModel.testSingleSuccess()
    }

    @Test
    fun testSingleFail() {
        viewModel.testSingleFail()
    }

    @Test
    fun testObservableSuccess() {
        viewModel.testObservableSuccess()
    }

    @Test
    fun testSequentialSetStatesWithinWithStateBlock() {
        viewModel.testSequentialSetStatesWithinWithStateBlock()
    }

    @Test
    fun testObservableWithMapper() {
        viewModel.testObservableWithMapper()
    }

    @Test
    fun testObservableFail() {
        viewModel.testObservableFail()
    }

    @Test
    fun testSubscribeNotCalledInInitialized() {
        owner.lifecycle.markState(Lifecycle.State.INITIALIZED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        assertEquals(0, callCount)
    }

    @Test
    fun testSubscribeNotCalledInCreated() {
        owner.lifecycle.markState(Lifecycle.State.CREATED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        assertEquals(0, callCount)
    }

    @Test
    fun testSubscribeCalledInStarted() {
        owner.lifecycle.markState(Lifecycle.State.STARTED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeCalledInResumed() {
        owner.lifecycle.markState(Lifecycle.State.RESUMED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeNotCalledInDestroyed() {
        owner.lifecycle.markState(Lifecycle.State.DESTROYED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        assertEquals(0, callCount)
    }

    @Test
    fun testSubscribeNotCalledWhenTransitionedToStopped() {
        owner.lifecycle.markState(Lifecycle.State.RESUMED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        viewModel.setFoo(1)

        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

        viewModel.setFoo(2)

        assertEquals(2, callCount)
    }

    @Test
    fun testSubscribeNotCalledWhenTransitionedToDestroyed() {
        owner.lifecycle.markState(Lifecycle.State.RESUMED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        viewModel.setFoo(1)

        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        viewModel.setFoo(2)

        assertEquals(2, callCount)
    }

    @Test
    fun testSubscribeCalledWhenTransitionToStarted() {
        owner.lifecycle.markState(Lifecycle.State.CREATED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        assertEquals(0, callCount)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeCalledWhenTransitionToResumed() {
        owner.lifecycle.markState(Lifecycle.State.STARTED)

        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }

        viewModel.setFoo(1)

        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        viewModel.setFoo(2)

        assertEquals(3, callCount)
    }

    @Test
    fun testSubscribeCalledOnRestart() {
        owner.lifecycle.markState(Lifecycle.State.RESUMED)
        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }
        assertEquals(1, callCount)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        assertEquals(1, callCount)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        assertEquals(1, callCount)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(2, callCount)
    }

    @Test
    fun testUniqueOnlySubscribeCalledOnStartIfUpdateOccurredInStop() {
        owner.lifecycle.markState(Lifecycle.State.STARTED)

        var callCount = 0
        viewModel.subscribe(owner, uniqueOnly = true) {
            callCount++
        }

        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

        viewModel.setFoo(1)
        assertEquals(1, callCount)

        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(2, callCount)
    }

    @Test
    fun testSubscribeNotCalledOnStartIfNoUpdateOccurredInStop() {
        owner.lifecycle.markState(Lifecycle.State.STARTED)

        var callCount = 0
        viewModel.subscribe(owner, uniqueOnly = true) {
            callCount++
        }

        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        assertEquals(1, callCount)

        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(1, callCount)
    }

    @Test
    fun testAsync() {
        var callCount = 0
        val success = "Hello World"
        val fail = IllegalStateException("Uh oh")
        viewModel.asyncSubscribe(owner, ViewModelTestState::async, onFail = {
            callCount++
            assertEquals(fail, it)
        }) {
            callCount++
            assertEquals(success, it)
        }
        viewModel.setAsync(Success(success))
        viewModel.setAsync(Fail(fail))
        assertEquals(2, callCount)
    }

    @Test
    fun testAddToList() {
        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }
        assertEquals(1, callCount)

        viewModel.set { copy(list = list + 5) }

        assertEquals(2, callCount)
    }

    @Test
    fun testReplace() {
        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }
        assertEquals(1, callCount)

        viewModel.set { copy(list = listOf(5)) }

        assertEquals(2, callCount)
    }

    @Test
    fun testChangeValue() {
        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }
        assertEquals(1, callCount)

        viewModel.set { copy(list = listOf(5)) }

        assertEquals(2, callCount)

        viewModel.set { copy(list = list.toMutableList().apply { set(0, 3) }) }

        assertEquals(3, callCount)
    }

    @Test
    fun testNoEventEmittedIfSameStateIsSet() {
        var callCount = 0
        viewModel.subscribe(owner) {
            callCount++
        }
        assertEquals(1, callCount)

        viewModel.set { copy() }
        assertEquals(1, callCount)

    }
}