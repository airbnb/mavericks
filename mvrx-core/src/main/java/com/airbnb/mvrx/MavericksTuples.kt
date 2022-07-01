package com.airbnb.mvrx

@InternalMavericksApi data class MavericksTuple1<A>(val a: A)
@InternalMavericksApi data class MavericksTuple2<A, B>(val a: A, val b: B)
@InternalMavericksApi data class MavericksTuple3<A, B, C>(val a: A, val b: B, val c: C)
@InternalMavericksApi data class MavericksTuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
@InternalMavericksApi data class MavericksTuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
@InternalMavericksApi data class MavericksTuple6<A, B, C, D, E, F>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E,
    val f: F
)
@InternalMavericksApi data class MavericksTuple7<A, B, C, D, E, F, G>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E,
    val f: F,
    val g: G
)
