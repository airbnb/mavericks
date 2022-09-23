package com.airbnb.mvrx.compose

import android.os.Parcel
import android.os.Parcelable
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel

data class CounterState(
    val count: Int = 0,
    val count2: Int = 123,
) : MavericksState {
    constructor(arguments: ArgumentsTest) : this(count = arguments.count)
}

class CounterViewModel(initialState: CounterState) : MavericksViewModel<CounterState>(initialState) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

data class ArgumentsTest(val count: Int) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ArgumentsTest> {
        override fun createFromParcel(parcel: Parcel): ArgumentsTest {
            return ArgumentsTest(parcel)
        }

        override fun newArray(size: Int): Array<ArgumentsTest?> {
            return arrayOfNulls(size)
        }
    }
}
