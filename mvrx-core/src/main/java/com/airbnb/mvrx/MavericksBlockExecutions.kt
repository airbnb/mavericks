package com.airbnb.mvrx

/**
 * Defines whether a [MavericksRepository.execute] invocation should not be run.
 */
enum class MavericksBlockExecutions {
    /** Run the execute block normally. */
    No,

    /** Block the execute call from having an impact. */
    Completely,

    /**
     * Block the execute call from having an impact from values returned by the object
     * being executed, but perform one state callback to set the Async property to loading
     * as if the call is actually happening.
     */
    WithLoading
}
