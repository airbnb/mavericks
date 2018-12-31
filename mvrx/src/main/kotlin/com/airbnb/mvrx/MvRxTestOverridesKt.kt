package com.airbnb.mvrx

import android.annotation.SuppressLint
import android.support.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
object MvRxTestOverridesKt {
    @SuppressLint("RestrictedApi")
    fun <S : MvRxState> getStateForTestRuleDoNotCallOnYourOwn(viewModel: BaseMvRxViewModel<S>): S {
        return viewModel.getStateForTestRuleDoNotCallOnYourOwn()
    }
}