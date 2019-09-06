package com.airbnb.mvrx

import android.os.Bundle
import java.util.UUID
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MvRxViewId : ReadOnlyProperty<MvRxView, String> {

    private var value: String? = null

    override fun getValue(thisRef: MvRxView, property: KProperty<*>): String {
        return value ?: generateUniqueId(thisRef).also { value = it }
    }

    private fun generateUniqueId(thisRef: MvRxView) = thisRef::class.java.simpleName + "_" + UUID.randomUUID().toString()

    fun saveTo(bundle: Bundle) {
        bundle.putString(PERSISTED_VIEW_ID_KEY, value)
    }

    fun restoreFrom(bundle: Bundle?) {
        if (value == null) {
            value = bundle?.getString(PERSISTED_VIEW_ID_KEY)
        }
    }

    companion object {
        private const val PERSISTED_VIEW_ID_KEY = "mvrx:persisted_view_id"
    }
}
