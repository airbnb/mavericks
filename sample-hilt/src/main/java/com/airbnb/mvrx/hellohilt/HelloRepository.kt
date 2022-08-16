package com.airbnb.mvrx.hellohilt

import javax.inject.Inject

class HelloRepository @Inject constructor(
    val viewModelScopedClass: ViewModelScopedClass,
    val notViewModelScopedClass: NotViewModelScopedClass,
)
