package com.airbnb.mvrx

import androidx.appcompat.app.AppCompatActivity

/**
 * Extend this class to get MvRx support out of the box.
 *
 * The purpose of this class is to be the host of MvRxFragments.
 * MvRxFragments are the screen unit in MvRx. Activities are meant
 * to just be the shell for your Fragments. There should be no business logic in your
 * Activities anymore. Use activityViewModel to share state between screens.
 *
 * To integrate this into your app. you may:
 * 1) Extend this directly.
 * 2) Replace your BaseActivity super class with this one.
 * 3) Manually integrate this into your base Activity (not recommended).
 */
abstract class BaseMvRxActivity : AppCompatActivity()