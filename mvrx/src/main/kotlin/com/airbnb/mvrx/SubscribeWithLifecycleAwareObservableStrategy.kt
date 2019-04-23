package com.airbnb.mvrx

import androidx.lifecycle.LifecycleOwner
import io.reactivex.functions.Consumer
import java.util.concurrent.ConcurrentHashMap

internal class SubscribeWithLifecycleAwareObservableStrategy(
    private val lifecycleOwner: LifecycleOwner,
    private val deliveryMode: DeliveryMode,
    private val lastDeliveredStates: ConcurrentHashMap<String, Any>,
    private val activeSubscriptions: MutableSet<String>
) : SubscribeWithStrategy {
    override fun <T : Any> subscribe(subscriber: (T) -> Unit) = MvRxLifecycleAwareObserver(
        lifecycleOwner,
        deliveryMode = deliveryMode,
        lastDeliveredValue = if (deliveryMode is UniqueOnly) {
            if (activeSubscriptions.contains(deliveryMode.subscriptionId)) {
                throw IllegalStateException(
                    "Subscribing with a duplicate subscription id: ${deliveryMode.subscriptionId}. " +
                            "If you have multiple uniqueOnly subscriptions in a MvRx view that listen to the same properties " +
                            "you must use a custom subscription id. If you are using a custom MvRxView, make sure you are using the proper" +
                            "lifecycle owner. See BaseMvRxFragment for an example."
                )
            }
            activeSubscriptions.add(deliveryMode.subscriptionId)
            lastDeliveredStates[deliveryMode.subscriptionId] as? T
        } else {
            null
        },
        onNext = Consumer { value ->
            if (deliveryMode is UniqueOnly) {
                lastDeliveredStates[deliveryMode.subscriptionId] = value
            }
            subscriber(value)
        },
        onDispose = {
            if (deliveryMode is UniqueOnly) {
                activeSubscriptions.remove(deliveryMode.subscriptionId)
            }
        }
    )
}