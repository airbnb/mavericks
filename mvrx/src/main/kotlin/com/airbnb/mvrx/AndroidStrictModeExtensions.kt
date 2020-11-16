package com.airbnb.mvrx

import android.os.StrictMode
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Wraps [StrictMode.ThreadPolicy] into [ThreadContextElement]. The resulting [ThreadContextElement] maintains the given policy
 * for coroutine regardless of the actual thread its is resumed on.
 */
fun StrictMode.ThreadPolicy.asContextElement(): CoroutineContext.Element = ThreadPolicyElement(this)

private class ThreadPolicyElement(val policy: StrictMode.ThreadPolicy) : ThreadContextElement<StrictMode.ThreadPolicy>,
    AbstractCoroutineContextElement(ThreadPolicyElement) {
    companion object Key : CoroutineContext.Key<ThreadPolicyElement>

    override fun restoreThreadContext(context: CoroutineContext, oldState: StrictMode.ThreadPolicy) {
        StrictMode.setThreadPolicy(oldState)
    }

    override fun updateThreadContext(context: CoroutineContext): StrictMode.ThreadPolicy {
        val oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(policy)
        return oldPolicy
    }
}
