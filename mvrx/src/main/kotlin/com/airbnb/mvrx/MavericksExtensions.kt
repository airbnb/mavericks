package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment

/**
 * For internal use only. Public for inline.
 *
 * Looks for [MvRx.KEY_ARG] on the arguments of the fragments.
 */
@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T : Fragment> T._fragmentArgsProvider(): Any? = arguments?.get(MvRx.KEY_ARG)
