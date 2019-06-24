package com.airbnb.mvrx

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent

class ViewLifecycleOwner(private val view: View) : LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    /**
     * This is guaranteed to be a MvRxView.
     */
    private var _fragment: Fragment? = null
    val fragment: Fragment?
        get() {
            // If a ViewModel is called from onAttachedToWindow, ths Fragment may be attached before the onAttachStateChangeListener is called.
            if (_fragment == null && view.isAttachedToWindow) {
                handleAttachedToWindow()
            }
            return _fragment
        }
    val activity get() = view.context as? AppCompatActivity

    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }

            override fun onViewAttachedToWindow(v: View) {
                handleAttachedToWindow()
            }
        })

        // TODO: handle Activity lifecycle to handle screen off and backgrounding

        // This causes Views to get invalidated twice when they are attached to the window
        // We call onInvalidate directly immediately to ensure that the correct data is rendered
        // in the first frame. However, invalidate will get posted again as the lifecycle
        // transitions to onResume.
        lifecycleRegistry.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                (view as? StatefulView)?.onInvalidate()
            }
        })

        // TODO: pause and resume for visibility.
    }

    private fun handleAttachedToWindow() {
        val lifecycleOwner = view.fragment()
        this@ViewLifecycleOwner._fragment = lifecycleOwner
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getLifecycle() = lifecycleRegistry

    fun View.fragment(): Fragment? {
        _fragment?.let { previousFragment ->
            if (!hasFragmentChanged(previousFragment)) return previousFragment
        }

        val activity = context as? AppCompatActivity
        val viewFragmentMap = mutableMapOf<View, Fragment>()
        activity?.supportFragmentManager?.fragments?.forEach { it.collectViewMap(viewFragmentMap) }

        var view: View? = this
        while (view != null) {
            val fragment = viewFragmentMap[view]
            if (fragment != null && fragment is MvRxView) {
                this@ViewLifecycleOwner._fragment = fragment
                return fragment
            }
            view = view.parent as? ViewGroup
        }

        return null
    }

    fun hasFragmentChanged(fragment: Fragment): Boolean {
        fragment.view?.let { previousFragmentView ->
            var view: View? = view
            while (view != null) {
                if (view == previousFragmentView) {
                    return false
                }
                view = view.parent as? ViewGroup
            }
        }
        return true
    }

    fun Fragment.collectViewMap(map: MutableMap<View, Fragment>) {
        view?.let { view -> map[view] = this }
        childFragmentManager.fragments.forEach { it.collectViewMap(map) }
    }
}