package com.airbnb.mvrx.sample.anvil.feature

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.anvil.R
import com.airbnb.mvrx.sample.anvil.UserScopedRepository
import com.airbnb.mvrx.sample.anvil.annotation.ContributesViewModel
import com.airbnb.mvrx.sample.anvil.databinding.HelloFragmentBinding
import com.airbnb.mvrx.sample.anvil.di.DaggerComponentOwner
import com.airbnb.mvrx.sample.anvil.di.bindings
import com.airbnb.mvrx.sample.anvil.di.daggerMavericksViewModelFactory
import com.airbnb.mvrx.sample.anvil.di.fragmentComponent
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

data class ExampleFeatureState(
    val title: Async<String> = Uninitialized,
    val description: Async<String> = Uninitialized,
) : MavericksState

/**
 * The following code and the companion object contain everything needed to wire up constructor injection with Anvil.
 *
 * The `sample-anvilcodegen` module contains the code generation that happens for [ContributesViewModel].
 *
 * Note that this ViewModel is created in [ExampleFeatureFragment] which is a [DaggerComponentOwner] for [ExampleFeatureComponent] which means
 * that this can inject anything from [ExampleFeatureComponent] as well. And because [ExampleFeatureComponent] is a subcomponent of
 * [com.airbnb.mvrx.sample.anvil.UserComponent] and [com.airbnb.mvrx.sample.anvil.AppComponent], it can inject anything from those components as well.
 */
@ContributesViewModel(ExampleFeatureScope::class)
class ExampleFeatureViewModel @AssistedInject constructor(
    @Assisted initialState: ExampleFeatureState,
    userScopedRepo: UserScopedRepository,
    featureScopedRepo: ExampleFeatureScopedRepository,
) : MavericksViewModel<ExampleFeatureState>(initialState) {

    init {
        suspend {
            userScopedRepo()
        }.execute { copy(title = it) }
        suspend {
            featureScopedRepo()
        }.execute { copy(description = it) }
    }

    companion object : MavericksViewModelFactory<ExampleFeatureViewModel, ExampleFeatureState> by daggerMavericksViewModelFactory()
}

class ExampleFeatureFragment : Fragment(R.layout.hello_fragment), MavericksView, DaggerComponentOwner {
    private val binding: HelloFragmentBinding by viewBinding()
    private val viewModel: ExampleFeatureViewModel by fragmentViewModel()

    /**
     * We are using this Fragment as the owner of a Dagger Component. In a real world example, this Fragment
     * could have child fragments and/or be a container for an entire flow or large feature.
     * With the [bindings] methods, any ViewModels for this fragment or any child fragments can inject objects
     * from this component.
     *
     * If you don't need a custom dagger component for your Fragment, you can omit this and the [DaggerComponentOwner] interface entirely.
     * In that case, you could contribute the ViewModel to the user or app component depending on what is available from
     * parent fragments/Activity/Application.
     */
    override val daggerComponent by fragmentComponent { scope, _ ->
        // Note: use `requireActivity().bindings` not `bindings` here or else you will wind up with a StackOverflow in which this
        // Fragment which is a DaggerComponentOwner will keep searching itself over and over.
        requireActivity().bindings<ExampleFeatureComponent.ParentBindings>().exampleFeatureComponentBuilder()
            .coroutineScope(ExampleFeatureCoroutineScope(scope))
            .build()
    }

    override fun invalidate() = withState(viewModel) { state ->
        requireActivity().title = state.description()
        binding.title.text = when (state.title) {
            is Uninitialized, is Loading -> getString(R.string.hello_fragment_loading_text)
            is Success -> state.title()
            is Fail -> getString(R.string.hello_fragment_failure_text)
        }
    }
}
