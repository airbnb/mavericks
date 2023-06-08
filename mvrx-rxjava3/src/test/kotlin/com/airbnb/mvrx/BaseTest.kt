package com.airbnb.mvrx

import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.test.MavericksTestRule
import org.junit.Ignore
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Ignore("Base Class")
abstract class BaseTest {

    @get:Rule
    val mvrxRule = MavericksTestRule(
        setForceDisableLifecycleAwareObserver = false,
        viewModelMockBehavior = MockBehavior(
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        ),
    )
}
