@file:Suppress("DEPRECATION")

package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.BaseMvRxFragment

class TestRuleView : BaseMvRxFragment() {

    var subscribeCallCount = 0

    private val viewModel = TestRuleViewModel()

    init {
        viewModel.subscribe {
            subscribeCallCount++
        }
    }

    override fun invalidate() {}

    fun doSomething() = viewModel.doSomething()
}