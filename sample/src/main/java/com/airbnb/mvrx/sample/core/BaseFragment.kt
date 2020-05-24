package com.airbnb.mvrx.sample.core

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView

abstract class BaseFragment(@LayoutRes containerLayoutId: Int = 0) : Fragment(containerLayoutId), MavericksView