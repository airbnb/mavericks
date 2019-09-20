package com.airbnb.mvrx.launcher.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment

internal fun Fragment.toastShort(str: String) {
    Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
}

internal fun Fragment.toastLong(str: String) {
    requireContext().toastLong(str)
}

internal fun Context.toastLong(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_LONG).show()
}

/**
 * A simple way to create an intent.
 *
 * Examples:
 *
 * `context.intent<MyActivity>() // no extras`
 *
 * `context.intent<MyActivity> { putExtra(EXTRA_ID, id) }`
 *
 * @param initializer An optional lambda where you can apply change to the new Intent object. Useful for setting extras.
 */
internal inline fun <reified T> Context.buildIntent(initializer: Intent.() -> Unit = {}): Intent {
    return Intent(this, T::class.java).apply(initializer)
}

internal fun View.dismissSoftKeyboard() {
    val windowToken = windowToken ?: return
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
        ?.hideSoftInputFromWindow(windowToken, 0)
}
