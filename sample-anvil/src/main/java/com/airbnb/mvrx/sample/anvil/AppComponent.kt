package com.airbnb.mvrx.sample.anvil

import com.airbnb.mvrx.sample.anvil.di.SingleIn
import com.squareup.anvil.annotations.MergeComponent

interface AppScope

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface AppComponent
