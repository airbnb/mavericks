package com.airbnb.mvrx

@Deprecated(
    message = "MvRx has been replaced with Mavericks",
    replaceWith = ReplaceWith("MavericksViewModelFactory<VM, S>")
)
interface MvRxViewModelFactory<VM : MavericksViewModel<S>, S : MavericksState> : MavericksViewModelFactory<VM, S>
