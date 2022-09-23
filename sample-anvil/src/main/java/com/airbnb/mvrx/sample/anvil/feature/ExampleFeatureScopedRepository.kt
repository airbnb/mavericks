package com.airbnb.mvrx.sample.anvil.feature

import com.airbnb.mvrx.sample.anvil.di.SingleIn
import javax.inject.Inject

@SingleIn(ExampleFeatureScope::class)
class ExampleFeatureScopedRepository @Inject constructor() {
    @Suppress("FunctionOnlyReturningConstant")
    operator fun invoke() = "Example Feature"
}
