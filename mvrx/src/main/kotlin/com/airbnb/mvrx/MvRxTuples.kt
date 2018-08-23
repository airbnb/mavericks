package com.airbnb.mvrx

class MvRxTuple1<A>(val a: A) {
    operator fun component1() = a
}
class MvRxTuple2<A, B>(val a: A, val b: B) {
    operator fun component1() = a
    operator fun component2() = b
}
class MvRxTuple3<A, B, C>(val a: A, val b: B, val c: C) {
    operator fun component1() = a
    operator fun component2() = b
    operator fun component3() = c
}
class MvRxTuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D) {
    operator fun component1() = a
    operator fun component2() = b
    operator fun component3() = c
    operator fun component4() = d
}