package com.airbnb.mvrx.news

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.mvrx.*
import com.airbnb.mvrx.news.base.BaseFragment
import com.airbnb.mvrx.news.base.simpleController
import com.airbnb.mvrx.news.di.appComponent
import com.airbnb.mvrx.news.views.articleView
import com.airbnb.mvrx.news.views.errorView
import com.airbnb.mvrx.news.views.loadingView
import javax.inject.Inject

class HeadlinesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: HeadlinesViewModel.Factory

    private val viewModel: HeadlinesViewModel by fragmentViewModel()

    override fun onAttach(context: Context) {
        appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        return root
    }

    override fun epoxyController() = simpleController(viewModel) { state ->

        state.headlines.forEach { article ->
            articleView {
                id(article.url)
                article(article)
                onArticleClick { _ -> launchBrowser(article.url) }
            }
        }

        when (state.headlinesRequest) {

            is Uninitialized, is Loading -> loadingView { id("loading") }

            is Fail -> errorView {
                id("error")
                message(R.string.errorMessage)
            }

            is Success -> {
                if (state.hasMoreArticles) {
                    loadingView {
                        id("load-more-articles")
                        onBind { _, _, _ -> viewModel.getMoreHeadlines() }
                    }
                } else {
                    errorView {
                        id("no-more-articles")
                        message(R.string.noMoreArticlesMessage)
                    }
                }
            }
        }
    }

    private fun launchBrowser(url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(requireContext(), Uri.parse(url))
    }
}