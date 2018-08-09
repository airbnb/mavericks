package com.airbnb.mvrx

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Parcelable class designed to hold the class name and saved state of a ViewModel so that it can be recreated
 * in a new process.
 *
 * @see MvRxViewModelStore
 */
@SuppressLint("ParcelCreator")
@Parcelize data class MvRxPersistedViewModelHolder(val viewModelClass: String, val stateClass: String, val state: Bundle) : Parcelable