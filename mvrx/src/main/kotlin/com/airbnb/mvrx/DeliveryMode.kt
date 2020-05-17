package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import kotlin.reflect.KProperty1

/**
 * Defines what updates a subscription should receive.
 * See: [RedeliverOnStart], [UniqueOnly].
 */
sealed class DeliveryMode {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun appendPropertiesToId(vararg properties: KProperty1<*, *>): DeliveryMode {
        return when (this) {
            is RedeliverOnStart -> RedeliverOnStart
            is UniqueOnly -> UniqueOnly(subscriptionId + "_" + properties.joinToString(",") { it.name })
        }
    }
}

/**
 * The subscription will receive the most recent state update when transitioning from locked to unlocked states (stopped -> started),
 * even if the state has not changed while locked.
 *
 * Likewise, when a MvRxView resubscribes after a configuration change the most recent update will always be emitted.
 */
object RedeliverOnStart : DeliveryMode()

/**
 * The subscription will receive the most recent state update when transitioning from locked to unlocked states (stopped -> started),
 * only if the state has changed while locked.
 *
 * Likewise, when a MvRxView resubscribes after a configuration change the most recent update will only be emitted
 * if the state has changed while locked.
 *
 * @param subscriptionId A uniqueIdentifier for this subscription. It is an error for two unique only subscriptions to
 * have the same id.
 */
class UniqueOnly(val subscriptionId: String) : DeliveryMode()