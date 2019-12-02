package com.airbnb.mvrx.persiststate

/**
 * Annotate a field in your MvRxViewModel state with [PersistState] to have it automatically persisted when Android kills your process
 * to free up memory. MvRx will automatically recreate your ViewModel when the process restarts with these fields saved.
 *
 * You should ONLY SAVE what you need to refetch data, not fetched data itself. For example, for search, save the search filters not the
 * search results.
 *
 * You can also only annotate [Serializable] and [android.os.Parcelable] fields.
 *
 * An example state class could look like:
 * data class State(@PersistState val count: Int = 0)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PersistState