package com.airbnb.mvrx.sample.anvil

import com.airbnb.mvrx.sample.anvil.di.SingleIn
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.BindsInstance
import dagger.Subcomponent

interface UserScope

data class User(val name: String)

@SingleIn(UserScope::class)
@MergeSubcomponent(UserScope::class)
interface UserComponent {
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun user(user: User): Builder
        fun build(): UserComponent
    }

    @ContributesTo(AppScope::class)
    interface ParentBindings {
        fun userComponentBuilder(): Builder
    }
}