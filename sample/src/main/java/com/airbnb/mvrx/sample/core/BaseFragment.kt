package com.airbnb.mvrx.sample.core

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.sample.R

abstract class BaseFragment : BaseMvRxFragment() {

    @Suppress("MemberVisibilityCanBePrivate")
    protected lateinit var recyclerView: EpoxyRecyclerView
    protected lateinit var toolbar: Toolbar
    protected lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_base_mvrx, container, false).apply {
            recyclerView = findViewById(R.id.recycler_view)
            toolbar = findViewById(R.id.toolbar)
            coordinatorLayout = findViewById(R.id.coordinator_layout)

            recyclerView.buildModelsWith {
                it.buildModels()
            }
            toolbar.setupWithNavController(findNavController())
        }
    }

    override fun invalidate() {
        recyclerView.requestModelBuild()
    }

    abstract fun EpoxyController.buildModels()

    protected fun navigateTo(@IdRes actionId: Int, arg: Parcelable? = null) {
        /**
         * If we put a parcelable arg in [MvRx.KEY_ARG] then MvRx will attempt to call a secondary
         * constructor on any MvRxState objects and pass in this arg directly.
         * @see [com.airbnb.mvrx.sample.features.dadjoke.DadJokeDetailState]
         */
        val bundle = arg?.let { Bundle().apply { putParcelable(MvRx.KEY_ARG, it) } }
        findNavController().navigate(actionId, bundle)
    }
}