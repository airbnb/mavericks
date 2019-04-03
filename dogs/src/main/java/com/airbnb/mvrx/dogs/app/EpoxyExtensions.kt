package com.airbnb.mvrx.dogs.app

import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView

/**
 * Easily add models to an EpoxyRecyclerView, the same way you would in a buildModels method of EpoxyController.
 * from https://github.com/airbnb/epoxy/wiki/EpoxyRecyclerView#kotlin-extensions
 */
fun EpoxyRecyclerView.withModels(buildModelsCallback: EpoxyController.() -> Unit) {
    setControllerAndBuildModels(object : EpoxyController() {
        override fun buildModels() {
            buildModelsCallback()
        }
    })
}