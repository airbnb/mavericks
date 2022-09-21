package com.airbnb.mvrx.sample.anvil

import android.os.Bundle
import android.view.View
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
import com.airbnb.mvrx.sample.anvil.di.daggerMavericksViewModelFactory
import com.airbnb.mvrx.mocking.MockableMavericksView
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState
import com.airbnb.mvrx.sample.anvil.annotation.ContributesViewModel
import com.airbnb.mvrx.sample.anvil.databinding.HelloFragmentBinding
import com.airbnb.mvrx.sample.anvil.di.DaggerComponentOwner
import com.airbnb.mvrx.sample.anvil.di.DaggerMavericksBindings
import com.airbnb.mvrx.sample.anvil.di.SingleIn
import com.airbnb.mvrx.sample.anvil.di.bindings
import com.airbnb.mvrx.sample.anvil.di.fragmentComponent
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

interface ExampleFeatureScope

@SingleIn(ExampleFeatureScope::class)
@MergeSubcomponent(ExampleFeatureScope::class)
interface ExampleFeatureComponent : DaggerMavericksBindings {
    @ContributesTo(UserScope::class)
    interface ParentBindings {
        fun exampleFeatureComponent(): ExampleFeatureComponent
    }
}

@SingleIn(ExampleFeatureScope::class)
class ExampleFeatureScopedRepository @Inject constructor() {
    @Suppress("FunctionOnlyReturningConstant")
    operator fun invoke() = "Example Feature"
}

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

    override val daggerComponent by fragmentComponent { _, app ->
        app.bindings<ExampleFeatureComponent.ParentBindings>().exampleFeatureComponent()
    }
    private val binding: HelloFragmentBinding by viewBinding()
    private val viewModel: ExampleFeatureViewModel by fragmentViewModel()

    override fun invalidate() = withState(viewModel) { state ->
        requireActivity().title = state.description()
        binding.title.text = when (state.title) {
            is Uninitialized, is Loading -> getString(R.string.hello_fragment_loading_text)
            is Success -> state.title()
            is Fail -> getString(R.string.hello_fragment_failure_text)
        }
    }
}
