package com.airbnb.mvrx.sample.anvil.feature

import com.airbnb.mvrx.sample.anvil.UserComponent
import com.airbnb.mvrx.sample.anvil.UserScope
import com.airbnb.mvrx.sample.anvil.di.DaggerMavericksBindings
import com.airbnb.mvrx.sample.anvil.di.SingleIn
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.BindsInstance
import dagger.Subcomponent
import kotlinx.coroutines.CoroutineScope

/**
 * This should be used as the scope for any `@SingleIn(ExampleFeatureScope::class)` objects.
 *
 * The reason this is a named class rather than just binding `CoroutineScope` directly is that there will likely
 * be a CoroutineScope associated with each Dagger component in the hierarchy (AppComponent, UserComponent, ExampleFeatureComponent).
 * Using a named class is the best way to be explicit and ensure that there aren't duplicate bindings and that the correct
 * scope is always used.
 */
class ExampleFeatureCoroutineScope(private val parentScope: CoroutineScope) : CoroutineScope by parentScope

interface ExampleFeatureScope

/**
 * Any component that provides ViewModels via [com.airbnb.mvrx.sample.anvil.annotation.ContributesViewModel] should
 * implement [DaggerMavericksBindings].
 */
@SingleIn(ExampleFeatureScope::class)
@MergeSubcomponent(ExampleFeatureScope::class)
interface ExampleFeatureComponent : DaggerMavericksBindings {
    @Subcomponent.Builder
    interface Builder {
        /**
         * This CoroutineScope will have the same lifecycle as this component. Any objects annotated with
         * `@SingleIn(ExampleFeatureScope::class)` that need a CoroutineScope should use this.
         */
        @BindsInstance
        fun coroutineScope(coroutineScope: ExampleFeatureCoroutineScope): Builder
        fun build(): ExampleFeatureComponent
    }

    /**
     * This is a subcomponent of [UserComponent]. This tells [UserComponent] that it needs to be able to
     * provide the builder for [ExampleFeatureCoroutineScope].
     */
    @ContributesTo(UserScope::class)
    interface ParentBindings {
        fun exampleFeatureComponentBuilder(): Builder
    }
}
