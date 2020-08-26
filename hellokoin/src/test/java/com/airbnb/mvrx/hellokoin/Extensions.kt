package com.airbnb.mvrx.hellokoin

import org.hamcrest.CoreMatchers.instanceOf
import org.junit.rules.ExpectedException
import org.koin.core.KoinApplication
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module


fun KoinApplication.loadModule(moduleDeclaration: ModuleDeclaration): KoinApplication {
    return modules(module(moduleDeclaration = moduleDeclaration))
}

inline fun <reified T : Throwable> ExpectedException.expectCause() {
    expectCause(instanceOf<T>(T::class.java))
}