package com.airbnb.mvrx

import java.util.Arrays

/**
 * The T generic is unused for some classes but since it is sealed and useful for Success and Fail,
 * it should be on all of them.
 *
 * Complete: Success, Fail
 * ShouldLoad: Uninitialized, Fail
 */
sealed class Async<out T>(val complete: Boolean, val shouldLoad: Boolean) {

    /**
     * Returns the Success value or null.
     *
     * Can be invoked as an operator like: `yourProp()`
     */
    open operator fun invoke(): T? = null

    /**
     * Map Async of one type to another.
     * If you are mapping a network request to the data class inside of it. If you can map the observable (such as a network request), it is preferrable.
     */
    fun <V> map(mapper: T.() -> V) = when (this) {
        Uninitialized -> Uninitialized
        is Loading -> Loading()
        is Success -> Success(mapper(invoke()))
        is Fail -> Fail(error)
    }

    /**
     * Like [pick] but has a single value for Fail.
     *
     * @see pick
     */
    fun <V> pick(incomplete: V, fail: V, success: T.() -> V) = pick(incomplete, { fail }, success)

    /**
     * Like [pick] but uses the same case for unitialized and loading.
     *
     * @param incomplete A value to show when the Async value is uninitialized or loading
     *
     * @see pick
     */
    fun <V> pick(incomplete: V, fail: (Throwable) -> V, success: T.() -> V) = pick(incomplete, incomplete, fail, success)

    /**
     * Disambiguate between different async cases easily.
     *
     * @param uninitialized A value to show when the Async value is uninitialized.
     * @param loading A value to show when the Async value is loading.
     * @param fail A lambda that receives the throwable error as a parameter and should return a value.
     * @param fail A lambda that receives the success value of the Async as the receiver and should return a value.
     */
    fun <V> pick(uninitialized: V, loading: V, fail: (Throwable) -> V, success: T.() -> V) = when (this) {
        is Uninitialized -> uninitialized
        is Loading -> loading
        is Success -> invoke().success()
        is Fail -> fail.invoke(error)
    }

    companion object {
        /**
         * Helper to set metadata on a Success instance.
         * @see Success.metadata
         */
        fun <T> Success<*>.setMetadata(metadata: T) {
            this.metadata = metadata
        }

        /**
         * Helper to get metadata on a Success instance.
         * @see Success.metadata
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> Success<*>.getMetadata(): T? = this.metadata as T?
    }
}

object Uninitialized : Async<Nothing>(complete = false, shouldLoad = true), Incomplete

class Loading<out T> : Async<T>(complete = false, shouldLoad = false), Incomplete {
    override fun equals(other: Any?) = other is Loading<*>

    override fun hashCode() = "Loading".hashCode()
}

data class Success<out T>(private val value: T) : Async<T>(complete = true, shouldLoad = false) {

    override operator fun invoke(): T = value

    /**
     * Optional information about the value.
     * This is intended to support tooling (eg logging).
     * It allows data about the original Observable to be kept and accessed later. For example,
     * you could map a network request to just the data you need in the value, but your base layers could
     * keep metadata about the request, like timing, for logging.
     *
     * @see BaseMvRxViewModel.execute
     * @see Async.setMetadata
     * @see Async.getMetadata
     */
    internal var metadata: Any? = null
}

data class Fail<out T>(val error: Throwable) : Async<T>(complete = true, shouldLoad = true) {
    override fun equals(other: Any?): Boolean {
        if (other !is Fail<*>) return false

        val otherError = other.error
        return error::class == otherError::class &&
                error.message == otherError.message &&
                error.stackTrace[0] == otherError.stackTrace[0]
    }

    override fun hashCode(): Int = Arrays.hashCode(arrayOf(error::class, error.message, error.stackTrace[0]))
}

/**
 * Helper interface for using Async in a when clause for handling both Uninitialized and Loading.
 *
 * With this, you can do:
 * when (data) {
 *     is Incomplete -> Unit
 *     is Success    -> Unit
 *     is Fail       -> Unit
 * }
 */
interface Incomplete
