package com.airbnb.mvrx

/**
 * Exists for backwards compatibility.
 *
 * @see Mavericks
 */
object MvRx {
    /**
     * @see Mavericks.KEY_ARG
     */
    @Deprecated(
        message = "MvRx has been replaced with Mavericks",
        replaceWith = ReplaceWith("Mavericks.KEY_ARG")
    )
    const val KEY_ARG = Mavericks.KEY_ARG
}
