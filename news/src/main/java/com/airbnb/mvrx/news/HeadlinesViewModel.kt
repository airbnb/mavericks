package com.airbnb.mvrx.news

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.news.base.MvRxViewModel
import com.airbnb.mvrx.news.usecases.GetHeadlinesUseCase
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 * The ViewModel needs access to the initialState parameter, which is available at runtime only.
 * Therefore we can not inject it normally with Dagger. Instead, we use AssistedInject, which
 * generates a factory for this ViewModel for us. This factory can be injected anywhere using Dagger,
 * and we can use it to create instances of our ViewModel.
 */
class HeadlinesViewModel @AssistedInject constructor(
        @Assisted initialState: HeadlinesState,
        private val headlinesUseCase: GetHeadlinesUseCase
) : MvRxViewModel<HeadlinesState>(initialState) {

    init {
        getMoreHeadlines()
    }

    fun getMoreHeadlines() = withState { state ->

        if (!state.hasMoreArticles) return@withState

        val pageToBeFetched = state.page + 1

        headlinesUseCase
                .getHeadlines(pageToBeFetched)
                .execute { request ->
                    val fetchedHeadlines = request()?.articles ?: emptyList()
                    copy(headlinesRequest = request, headlines = headlines + fetchedHeadlines, page = state.page + 1)
                }
    }

    /**
     * Assisted inject factory for this ViewModel
     */
    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: HeadlinesState): HeadlinesViewModel
    }

    companion object : MvRxViewModelFactory<HeadlinesViewModel, HeadlinesState> {
        /**
         * Since we rely on AssistedInject's generated factory to create this ViewModel for us,
         * we need to access it from the owning fragment.
         */
        override fun create(viewModelContext: ViewModelContext, state: HeadlinesState): HeadlinesViewModel? {
            val fragment = (viewModelContext as FragmentViewModelContext).fragment<HeadlinesFragment>()
            return fragment.viewModelFactory.create(state)
        }
    }
}