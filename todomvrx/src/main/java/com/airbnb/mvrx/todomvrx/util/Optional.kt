package com.airbnb.mvrx.todomvrx.util

data class Optional<T>(private val value: T?) {
    operator fun invoke(): T? = value
}