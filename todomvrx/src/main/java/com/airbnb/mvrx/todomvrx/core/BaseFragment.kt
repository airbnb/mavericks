package com.airbnb.mvrx.todomvrx.core

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.todomvrx.todoapp.R

abstract class BaseFragment : BaseMvRxFragment() {

    protected lateinit var recyclerView: EpoxyRecyclerView
    protected lateinit var fab: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_base, container, false).apply {
                recyclerView = findViewById(R.id.recycler_view)
                recyclerView.buildModelsWith { it.buildModels() }
                fab = findViewById(R.id.fab)
            }

    override fun invalidate() {
        recyclerView.requestModelBuild()
    }

    abstract fun EpoxyController.buildModels()
}