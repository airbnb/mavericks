package com.airbnb.mvrx

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.util.UUID

class MvRxViewIdViewModel(state: SavedStateHandle) : ViewModel() {
    val mvrxViewId = state[PERSISTED_VIEW_ID_KEY] ?: generateUniqueId().also { id ->
        state[PERSISTED_VIEW_ID_KEY] = id
    }

    private fun generateUniqueId() = "MvRxView_" + UUID.randomUUID().toString()

    companion object {
        private const val PERSISTED_VIEW_ID_KEY = "mvrx:persisted_view_id"
    }
}