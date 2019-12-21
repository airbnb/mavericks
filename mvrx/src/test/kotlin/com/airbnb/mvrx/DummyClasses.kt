package com.airbnb.mvrx

class A(val foo: Int = 5)

class B(val foo: Int = 5) {
    constructor(bar: String) : this(bar.toInt())
}