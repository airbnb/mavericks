package com.airbnb.mvrx.todomvrx.util

import android.os.Handler
import android.os.HandlerThread
import com.airbnb.epoxy.EpoxyController

class ToDoEpoxyController(val buildModelsCallback: EpoxyController.() -> Unit = {}) : EpoxyController(handler, handler) {
    override fun buildModels() {
        buildModelsCallback()
    }

    companion object {
        val handler = HandlerThread("epoxy").run {
            start()
            Handler(this.looper)
        }
    }
}