package com.airbnb.mvrx.sample.core

import android.support.v4.app.Fragment
import android.view.View
import kotlin.reflect.KProperty

fun <V : View> View.bindView(id: Int): ViewDelegate<View, V> = required(id, viewFinder)
fun <V : View> Fragment.bindView(id: Int): ViewDelegate<Fragment, V> = required(id, viewFinder)

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(id: Int, finder: T.(Int) -> View?) = ViewDelegate { t: T, kProperty ->
    t.finder(id) as V? ?: viewNotFound(
            id,
            kProperty
    )
}

private fun viewNotFound(id: Int, kProperty: KProperty<*>): Nothing = throw IllegalStateException("View ID $id for '${kProperty.name}' not found.")

@Suppress("unused")
private val View.viewFinder: View.(Int) -> View?
    get() = { this.findViewById(it) }

@Suppress("unused")
private val Fragment.viewFinder: Fragment.(Int) -> View?
    get() = { view!!.findViewById(it) }