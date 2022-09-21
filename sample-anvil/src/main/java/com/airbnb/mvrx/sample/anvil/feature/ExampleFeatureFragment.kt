package com.airbnb.mvrx.sample.anvil.feature

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.mocking.MockableMavericksView
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

@ContributesViewModel(ExampleFeatureScope::class)
class ExampleFeatureViewModel @AssistedInject constructor(
    @Assisted initialState: ExampleFeatureState,
    userScopedRepo: UserScopedRepository,
    featureScopedRepo: ExampleFeatureScopedRepository,
) : MavericksViewModel<ExampleFeatureState>(initialState) {

    init {
        suspend {
            userScopedRepo.helloWorld()
        }.execute { copy(title = it) }
        suspend {
            featureScopedRepo()
        }.execute { copy(description = it) }
    }

    companion object : MavericksViewModelFactory<ExampleFeatureViewModel, ExampleFeatureState> by daggerMavericksViewModelFactory()
}

class ExampleFeatureFragment : Fragment(R.layout.hello_fragment), MockableMavericksView, DaggerComponentOwner {
    private val binding: HelloFragmentBinding by viewBinding()
    private val viewModel: ExampleFeatureViewModel by fragmentViewModel()

    override val daggerComponent by fragmentComponent { scope, app ->
        app.bindings<ExampleFeatureComponent.ParentBindings>().exampleFeatureComponentBuilder()
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
