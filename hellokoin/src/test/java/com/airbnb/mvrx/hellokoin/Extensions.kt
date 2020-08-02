package com.airbnb.mvrx.hellokoin

import org.koin.core.KoinApplication
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module


fun KoinApplication.loadModule(moduleDeclaration: ModuleDeclaration): KoinApplication {
    return modules(module(moduleDeclaration = moduleDeclaration))
}