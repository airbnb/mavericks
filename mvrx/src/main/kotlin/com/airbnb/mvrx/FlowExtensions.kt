package com.airbnb.mvrx

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.yield
import java.util.concurrent.ConcurrentHashMap

internal fun <T : Any?> Flow<T>.collectLatest(
    lifecycleOwner: LifecycleOwner,
    lastDeliveredStates: ConcurrentHashMap<String, Any?>,
    activeSubscriptions: MutableSet<String>,
    deliveryMode: DeliveryMode,
    action: suspend (T) -> Unit
): Job {
    val flow = when {
        MavericksTestOverrides.FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER -> this
        deliveryMode is UniqueOnly -> {
            this.assertOneActiveSubscription(lifecycleOwner, activeSubscriptions, deliveryMode.subscriptionId)
                .dropWhile { lastDeliveredStates.containsKey(deliveryMode.subscriptionId) && it == lastDeliveredStates[deliveryMode.subscriptionId] }
                .flowWhenStarted(lifecycleOwner)
                .distinctUntilChanged()
                .onEach { lastDeliveredStates[deliveryMode.subscriptionId] = it }
        }
        else -> flowWhenStarted(lifecycleOwner)
    }

    val scope = lifecycleOwner.lifecycleScope + Mavericks.viewModelConfigFactory.subscriptionCoroutineContextOverride
    return scope.launch(start = CoroutineStart.UNDISPATCHED) {
        // Use yield to ensure flow collect coroutine is dispatched rather than invoked immediately.
        // This is necessary when Dispatchers.Main.immediate is used in scope.
        // Coroutine is launched with start = CoroutineStart.UNDISPATCHED to perform dispatch only once.
        yield()
        flow.collectLatest {
            if (MavericksTestOverrides.FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER) {
                action(it)
            } else {
                lifecycleOwner.whenStarted { action(it) }
            }
        }
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
internal fun <T : Any?> Flow<T>.assertOneActiveSubscription(
    lifecycleOwner: LifecycleOwner,
    activeSubscriptions: MutableSet<String>,
    subscriptionId: String
): Flow<T> {
    val observer = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            if (activeSubscriptions.contains(subscriptionId)) error(duplicateSubscriptionMessage(subscriptionId))
            activeSubscriptions += subscriptionId
        }

        override fun onDestroy(owner: LifecycleOwner) {
            activeSubscriptions.remove(subscriptionId)
        }
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    return onCompletion {
        activeSubscriptions.remove(subscriptionId)
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
}

private fun duplicateSubscriptionMessage(subscriptionId: String) = """
        Subscribing with a duplicate subscription id: $subscriptionId.
        If you have multiple uniqueOnly subscriptions in a Mavericks view that listen to the same properties
        you must use a custom subscription id. If you are using a custom MavericksView, make sure you are using the proper
        lifecycle owner. See BaseMvRxFragment for an example.
""".trimIndent()
