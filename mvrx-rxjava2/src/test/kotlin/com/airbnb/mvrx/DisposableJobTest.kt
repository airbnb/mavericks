package com.airbnb.mvrx

import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DisposableJobTest {
    private lateinit var job: CompletableJob
    private lateinit var disposable: Disposable

    @Before
    fun setup() {
        job = Job()
        disposable = job.toDisposable()
    }

    @Test
    fun `dispose() cancels job`() {
        disposable.dispose()
        assert(job.isCancelled)
        assert(disposable.isDisposed)
    }

    @Test
    fun `isDisposed reflects job cancellation`() {
        assert(!disposable.isDisposed)
        job.cancel()
        assert(disposable.isDisposed)
    }

    @Test
    fun `isDisposed reflects job completion`() {
        assert(!disposable.isDisposed)
        job.complete()
        assert(disposable.isDisposed)
    }
}
