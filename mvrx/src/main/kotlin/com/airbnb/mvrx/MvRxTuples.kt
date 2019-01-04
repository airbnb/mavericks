package com.airbnb.mvrx

data class MvRxTuple1<A>(val a: A)
data class MvRxTuple2<A, B>(val a: A, val b: B)
data class MvRxTuple3<A, B, C>(val a: A, val b: B, val c: C)
data class MvRxTuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
data class MvRxTuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
data class MvRxTuple6<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)
data class MvRxTuple7<A, B, C, D, E, F, G>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G)

/**
 * MvRxTuple1 is not so useful for [BaseMvRxViewModel.select]
 */
fun <A> tuple(a: A) = MvRxTuple1(a)
fun <A, B> tuple(a: A, b: B) = MvRxTuple2(a, b)
fun <A, B, C> tuple(a: A, b: B, c: C) = MvRxTuple3(a, b, c)
fun <A, B, C, D> tuple(a: A, b: B, c: C, d: D) = MvRxTuple4(a, b, c, d)
fun <A, B, C, D, E> tuple(a: A, b: B, c: C, d: D, e: E) = MvRxTuple5(a, b, c, d, e)
fun <A, B, C, D, E, F> tuple(a: A, b: B, c: C, d: D, e: E, f: F) = MvRxTuple6(a, b, c, d, e, f)
fun <A, B, C, D, E, F, G> tuple(a: A, b: B, c: C, d: D, e: E, f: F, g: G) = MvRxTuple7(a, b, c, d, e, f, g)

operator fun <A, B> MvRxTuple1<A>.plus(b: B) = tuple(a, b)
operator fun <A, B, C> MvRxTuple2<A, B>.plus(c: C) = tuple(a, b, c)
operator fun <A, B, C, D> MvRxTuple3<A, B, C>.plus(d: D) = tuple(a, b, c, d)
operator fun <A, B, C, D, E> MvRxTuple4<A, B, C, D>.plus(e: E) = tuple(a, b, c, d, e)
operator fun <A, B, C, D, E, F> MvRxTuple5<A, B, C, D, E>.plus(f: F) = tuple(a, b, c, d, e, f)
operator fun <A, B, C, D, E, F, G> MvRxTuple6<A, B, C, D, E, F>.plus(g: G) = tuple(a, b, c, d, e, f, g)
