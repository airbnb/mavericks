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
import com.airbnb.mvrx.anvil.AppScope
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.anvil.di.daggerMavericksViewModelFactory
import com.airbnb.mvrx.mocking.MockableMavericksView
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState
import com.airbnb.mvrx.sample.anvil.annotation.ContributesViewModel
import com.airbnb.mvrx.sample.anvil.databinding.HelloFragmentBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

data class HelloAnvilState(val message: Async<String> = Uninitialized) : MavericksState

@ContributesViewModel(AppScope::class)
class HelloAnvilViewModel @AssistedInject constructor(
    @Assisted initialState: HelloAnvilState,
    private val repo: HelloRepository
) : MavericksViewModel<HelloAnvilState>(initialState) {

    init {
        sayHello()
    }

    fun sayHello() {
        repo.sayHello().execute { copy(message = it) }
    }

    companion object : MavericksViewModelFactory<HelloAnvilViewModel, HelloAnvilState> by daggerMavericksViewModelFactory()
}


class HelloFragment : Fragment(R.layout.hello_fragment), MockableMavericksView {
    private val binding: HelloFragmentBinding by viewBinding()
    private val viewModel: HelloAnvilViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.helloButton.setOnClickListener { viewModel.sayHello() }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.helloButton.isEnabled = state.message !is Loading
        binding.messageTextView.text = when (state.message) {
            is Uninitialized, is Loading -> getString(R.string.hello_fragment_loading_text)
            is Success -> state.message()
            is Fail -> getString(R.string.hello_fragment_failure_text)
        }
    }
}
