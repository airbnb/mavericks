package com.airbnb.mvrx

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.reflect.KProperty1

// Set of MvRxView identity hash codes that have a pending invalidate.
private val pendingInvalidates = HashSet<Int>()
private val handler = Handler(Looper.getMainLooper(), Handler.Callback { message ->
    val view = message.obj as MavericksView
    pendingInvalidates.remove(System.identityHashCode(view))
    if (view.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) view.invalidate()
    true
})

interface MavericksView : LifecycleOwner {
    /**
     * Override this to supply a globally unique id for this MvRxView. If your MvRxView is being recreated due to
     * a lifecycle event (e.g. rotation) you should assign a consistent id. Likely this means you should save the id
     * in onSaveInstance state. The viewId will not be accessed until a subscribe method is called.
     * Accessing mvrxViewId before calling super.onCreate() will cause a crash.
     */
    val mvrxViewId: String get() = when (this) {
        is ViewModelStoreOwner -> ViewModelProvider(this).get(MvRxViewIdViewModel::class.java).mvrxViewId
        else -> error("If your MvRxView is not a ViewModelStoreOwner, you must implement mvrxViewId " +
            "and return a string that is unique to this view and persistant across its entire lifecycle.")
    }

    /**
     * Override this to handle any state changes from MvRxViewModels created through MvRx Fragment delegates.
     */
    fun invalidate()

    /**
     * The [LifecycleOwner] to use when making new subscriptions. You may want to return different owners depending
     * on what state your [MvRxView] is in. For fragments, subscriptions made in `onCreate` should use
     * the fragment's lifecycle owner so that the subscriptions are cleared in `onDestroy`. Subscriptions made in or after
     * `onCreateView` should use the fragment's _view's_ lifecycle owner so that they are cleared in `onDestroyView`.
     *
     * For example, if you are using a fragment as a MvRxView the proper implementation is:
     * ```
     *     override val subscriptionLifecycleOwner: LifecycleOwner
     *        get() = this.viewLifecycleOwnerLiveData.value ?: this
     * ```
     *
     * By default [subscriptionLifecycleOwner] is the same as the MvRxView's standard lifecycle owner.
     */
    val subscriptionLifecycleOwner: LifecycleOwner
        get() = (this as? Fragment)?.viewLifecycleOwnerLiveData?.value ?: this

    fun postInvalidate() {
        if (pendingInvalidates.add(System.identityHashCode(this@MavericksView))) {
            handler.sendMessage(Message.obtain(handler, System.identityHashCode(this@MavericksView), this@MavericksView))
        }
    }

    /**
     * Return a [UniqueOnly] delivery mode with a unique id for this fragment. In rare circumstances, if you
     * make two identical subscriptions with the same (or all) properties in this fragment, provide a customId
     * to avoid collisions.
     *
     * @param An additional custom id to identify this subscription. Only necessary if there are two subscriptions
     * in this fragment with exact same properties (i.e. two subscribes, or two selectSubscribes with the same properties).
     */
    fun uniqueOnly(customId: String? = null): UniqueOnly {
        return UniqueOnly(listOfNotNull(mvrxViewId, customId).joinToString("_"))
    }

    fun <S : MvRxState> BaseMavericksViewModel<S>.onEach(deliveryMode: DeliveryMode = RedeliverOnStart, action: suspend (S) -> Unit) =
        onEachInternal(this@MavericksView.subscriptionLifecycleOwner, deliveryMode, action)

    /**
     * Subscribes to all state updates for the given viewModel.
     *
     * @param deliveryMode If [UniqueOnly] when this MvRxView goes from a stopped to started lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState> BaseMavericksViewModel<S>.onEach(deliveryMode: DeliveryMode = RedeliverOnStart, action: (S) -> Unit) =
        onEachInternal(subscriptionLifecycleOwner, deliveryMode, { action(it) })

    /**
     * Subscribes to state changes for only a specific property and calls the subscribe with
     * only that single property.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A> BaseMavericksViewModel<S>.onEach(
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A) -> Unit
    ) = onEach1Internal(subscriptionLifecycleOwner, prop1, deliveryMode, { action(it) })

    /**
     * Subscribes to state changes for two properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B> BaseMavericksViewModel<S>.onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B) -> Unit
    ) = onEach2Internal(subscriptionLifecycleOwner, prop1, prop2, deliveryMode, action)

    /**
     * Subscribes to state changes for three properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B, C> BaseMavericksViewModel<S>.onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C) -> Unit
    ) = onEach3Internal(subscriptionLifecycleOwner, prop1, prop2, prop3, deliveryMode, action)

    /**
     * Subscribes to state changes for four properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B, C, D> BaseMavericksViewModel<S>.onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D) -> Unit
    ) = onEach4Internal(subscriptionLifecycleOwner, prop1, prop2, prop3, prop4, deliveryMode, action)

    /**
     * Subscribes to state changes for five properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B, C, D, E> BaseMavericksViewModel<S>.onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D, E) -> Unit
    ) = onEach5Internal(subscriptionLifecycleOwner, prop1, prop2, prop3, prop4, prop5, deliveryMode, action)

    /**
     * Subscribes to state changes for six properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B, C, D, E, F> BaseMavericksViewModel<S>.onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D, E, F) -> Unit
    ) = onEach6Internal(subscriptionLifecycleOwner, prop1, prop2, prop3, prop4, prop5, prop6, deliveryMode, action)

    /**
     * Subscribes to state changes for seven properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B, C, D, E, F, G> BaseMavericksViewModel<S>.onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        prop7: KProperty1<S, G>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D, E, F, G) -> Unit
    ) = onEach7Internal(subscriptionLifecycleOwner, prop1, prop2, prop3, prop4, prop5, prop6, prop7, deliveryMode, action)

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, T> BaseMavericksViewModel<S>.asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        onFail: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (T) -> Unit)? = null
    ) = onAsyncInternal(subscriptionLifecycleOwner, asyncProp, deliveryMode, onFail, onSuccess)
}