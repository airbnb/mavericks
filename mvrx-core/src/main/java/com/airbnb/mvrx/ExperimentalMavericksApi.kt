package com.airbnb.mvrx

/**
 * Marks declarations that are still experimental in Mavericks API.
 * Marked declarations are subject to change their semantics or behaviours, that not backward compatible.
 */
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class ExperimentalMavericksApi
