package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.MvRxState

inline class InlineClass(val value: Int)

data class TestInlineState(
    val inlineClass: InlineClass = InlineClass(5),
    val dogState: DogState = DogState()
) : MvRxState

fun foo() {
    TestInlineState().copy(inlineClass = InlineClass(7))
}
