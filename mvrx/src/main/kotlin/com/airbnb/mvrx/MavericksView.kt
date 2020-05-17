package com.airbnb.mvrx

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

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
}