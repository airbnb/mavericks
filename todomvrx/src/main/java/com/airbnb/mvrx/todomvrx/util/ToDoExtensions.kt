package com.airbnb.mvrx.todomvrx.util

import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar

fun <T> List<T>.upsert(value: T, finder: (T) -> Boolean) = indexOfFirst(finder).let { index ->
    if (index >= 0) copy(index, value) else this + value
}

fun <T> List<T>.copy(i: Int, value: T): List<T> = toMutableList().apply { set(i, value) }

inline fun <T> List<T>.delete(filter: (T) -> Boolean): List<T> = toMutableList().apply { removeAt(indexOfFirst(filter)) }

fun CoordinatorLayout.showLongSnackbar(@StringRes stringRes: Int) {
    Snackbar.make(this, stringRes, Snackbar.LENGTH_LONG).show()
}