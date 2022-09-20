package com.airbnb.mvrx.sample.anvil

import com.airbnb.mvrx.sample.anvil.di.SingleIn
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.BindsInstance
import dagger.Subcomponent

interface UserScope

@SingleIn(UserScope::class)
@MergeSubcomponent(UserScope::class)
interface UserComponent {
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun user(user: User): Builder
        fun build(): UserComponent
    }
}