package com.airbnb.mvrx.sample.core

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.MvRxViewModelStore
import com.airbnb.mvrx.sample.R
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class BaseMvRxFragment : Fragment(), MvRxView {

    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    protected lateinit var recyclerView: EpoxyRecyclerView
    protected lateinit var toolbar: Toolbar
    protected lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * This MUST be done to restore ViewModel state.
         */
        mvrxViewModelStore.restoreViewModels(this, savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_base_mvrx, container, false).apply {
            recyclerView = findViewById(R.id.recycler_view)
            toolbar = findViewById(R.id.toolbar)
            coordinatorLayout = findViewById(R.id.coordinator_layout)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.buildModelsWith {
            it.buildModels()
        }
        toolbar.setupWithNavController(findNavController())
    }

    override fun onStart() {
        super.onStart()
        // TODO: make this behavior automatic.
        invalidate()
    }

    /**
     *
     */
    override fun readyToInvalidate() = isAdded && view != null

    override fun invalidate() {
        recyclerView.requestModelBuild()
    }

    /**
     * Subscribes to all state updates for the given viewModel.
     *
     * Use shouldUpdate if you only want to subscribe to a subset of all updates. There are some standard ones in ShouldUpdateHelpers.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribe(
            shouldUpdate: ((S, S) -> Boolean)? = null,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: ((S) -> Unit)? = null
    ) = subscribe(this@BaseMvRxFragment, shouldUpdate, observerScheduler, subscriber ?: { if (readyToInvalidate()) invalidate() })

    /**
     * Subscribes to all state updates for the given viewModel. The subscriber will receive the previous state and the new state.
     *
     * Use shouldUpdate if you only want to subscribe to a subset of all updates. There are some standard ones in ShouldUpdateHelpers.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribeWithHistory(
            shouldUpdate: ((S, S) -> Boolean)? = null,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: (S, S) -> Unit
    ) = subscribeWithHistory(this@BaseMvRxFragment, shouldUpdate, observerScheduler, subscriber)

    open fun EpoxyController.buildModels() {

    }
}