package com.airbnb.mvrx.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import io.reactivex.disposables.Disposable
import kotlin.reflect.KProperty1

abstract class LauncherBaseFragment : BaseMvRxFragment() {

    protected lateinit var recyclerView: EpoxyRecyclerView
    protected lateinit var toolbar: Toolbar
    protected lateinit var coordinatorLayout: CoordinatorLayout
    protected val epoxyController by lazy { epoxyController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        epoxyController.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mvrx_fragment_base_launcher, container, false).apply {
            recyclerView = findViewById(R.id.recycler_view)
            toolbar = findViewById(R.id.toolbar)
            coordinatorLayout = findViewById(R.id.coordinator_layout)

            recyclerView.setController(epoxyController)

//            toolbar.setupWithNavController(findNavController())
        }
    }

    override fun invalidate() {
        recyclerView.requestModelBuild()
    }

    /**
     * Similar to [asyncSubscribe], but the success callback is only called once,
     * even if there are multiple success results (such as a request double response),
     * for the life of the fragment.
     *
     * If the fragment is recreated (eg, from activity recreation), and this callback is registered again,
     * it can be called with an existing Success value, even if a previous fragment instance also received the callback.
     * This is because it is impossible to know if a previous fragment instance already received a Success callback.
     *
     * This can be used for initialization that needs to happen only once after another request finishes (such as logging, or making other requests).
     */
    @SuppressLint("RestrictedApi")
    protected fun <V : BaseMvRxViewModel<S>, S : MvRxState, T> V.asyncFirstSuccess(
        asyncProp: KProperty1<S, Async<T>>,
        onSuccess: (T) -> Unit
    ) {
        var disposable: Disposable? = null
        disposable = asyncSubscribe(this@LauncherBaseFragment, asyncProp) {
            disposable?.dispose()
            onSuccess(it)
        }
    }

    /**
     * Provide the EpoxyController to use when building models for this Fragment.
     * Basic usages can simply use [simpleController]
     */
    abstract fun epoxyController(): EpoxyController

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        epoxyController.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        epoxyController.cancelPendingModelBuild()
        super.onDestroyView()
    }
}