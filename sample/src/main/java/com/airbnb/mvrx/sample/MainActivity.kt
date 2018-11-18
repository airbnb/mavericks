package com.airbnb.mvrx.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.airbnb.mvrx.ViewModelFactoryOwner
import com.airbnb.mvrx.viewModel

/**
 * Be the host of MvRxFragments. MvRxFragments are the screen unit in MvRx. Activities are meant
 * to just be the shell for your Fragments. There should be no business logic in yourActivities anymore.
 * Use activityViewModel to share state between screens.
 * You rarely need to implement [ViewModelFactoryOwner] in Activity unless you want to use [viewModel]
 * to create ViewModel directly in Activity. If you use activityViewModel to share state, the Activity do
 * *NOT* need to implement [ViewModelFactoryOwner] either, because the Fragment can provide the ViewModelFactory.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}