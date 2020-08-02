package com.airbnb.mvrx.hellokoin

import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelProvider
import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import com.airbnb.mvrx.hellokoin.di.KoinMvRxViewModelFactory
import com.airbnb.mvrx.hellokoin.di.KoinNoFactoryFoundException
import com.airbnb.mvrx.hellokoin.di.defaultScopeProvider
import com.airbnb.mvrx.hellokoin.di.scopeProvider
import com.airbnb.mvrx.test.MvRxTestRule
import com.airbnb.mvrx.withState
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

class GlobalDependency
class ScopedDependency

data class TestState(
    val int: Int = 0,
    val str: String = "test"
) : MvRxState

class TestViewModel constructor(
    state: TestState,
    private val globalDep: GlobalDependency
) : BaseViewModel<TestState>(state) {

    companion object : KoinMvRxViewModelFactory<TestViewModel, TestState>(TestViewModel::class.java)
}

data class ScopedTestState(
    val int: Int = 0,
    val str: String = "test"
) : MvRxState

class ScopedTestViewModel constructor(
    state: ScopedTestState,
    private val globalDep: GlobalDependency,
    private val scopedDep: ScopedDependency
) : BaseViewModel<ScopedTestState>(state) {

    companion object : KoinMvRxViewModelFactory<ScopedTestViewModel, ScopedTestState>(ScopedTestViewModel::class.java)
}

/**
 * Test integration between [Koin] an [MvRx] in [KoinMvRxViewModelFactory].
 */
@RunWith(RobolectricTestRunner::class)
class KoinMvRxViewModelFactoryTest : KoinTest {

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule()
    }

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    private lateinit var activity: FragmentActivity

    @Before
    fun setup() {
        activity = Robolectric.setupActivity(FragmentActivity::class.java)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun createFromGlobalScope() {
        startKoin {
            loadModule {
                factory { GlobalDependency() }
                viewModel { (state: TestState) -> TestViewModel(state, get()) }
            }
        }

        val viewModel = MvRxViewModelProvider.get(
            TestViewModel::class.java,
            TestState::class.java,
            ActivityViewModelContext(activity, null),
            TestViewModel::class.java.name
        )
        withState(viewModel) { state ->
            Assert.assertEquals(TestState(), state)
        }
    }

    @Test
    fun createFromActivityScope() {
        startKoin {
            loadModule {
                factory { GlobalDependency() }
                scope<FragmentActivity> {
                    scoped { ScopedDependency() }
                    viewModel { (state: ScopedTestState) -> ScopedTestViewModel(state, get(), get()) }
                }
            }
        }

        val viewModel = MvRxViewModelProvider.get(
            ScopedTestViewModel::class.java,
            ScopedTestState::class.java,
            ActivityViewModelContext(activity, null, customData = defaultScopeProvider),
            ScopedTestViewModel::class.java.name
        )
        withState(viewModel) { state ->
            Assert.assertEquals(ScopedTestState(), state)
        }
    }

    @Test
    fun createFromCustomScope() {
        startKoin {
            loadModule {
                factory { GlobalDependency() }
                scope(named("custom_scope")) {
                    scoped { ScopedDependency() }
                    viewModel { (state: ScopedTestState) -> ScopedTestViewModel(state, get(), get()) }
                }
            }
        }

        val testScope = getKoin().createScope("testScope", named("custom_scope"))

        val viewModel = MvRxViewModelProvider.get(
            ScopedTestViewModel::class.java,
            ScopedTestState::class.java,
            ActivityViewModelContext(activity, null, customData = scopeProvider { testScope }),
            ScopedTestViewModel::class.java.name
        )
        withState(viewModel) { state ->
            Assert.assertEquals(ScopedTestState(), state)
        }
    }

    @Test
    fun failToCreateFromOuterScope() {
        startKoin {
            loadModule {
                factory { GlobalDependency() }
                scope<FragmentActivity> {
                    scoped { ScopedDependency() }
                    viewModel { (state: ScopedTestState) -> ScopedTestViewModel(state, get(), get()) }
                }
            }
        }

        exception.expectCause<KoinNoFactoryFoundException>()
        MvRxViewModelProvider.get(
            ScopedTestViewModel::class.java,
            ScopedTestState::class.java,
            ActivityViewModelContext(activity, null),
            ScopedTestViewModel::class.java.name
        )
    }

    @Test
    fun failToCreateFromInnerScope() {
        startKoin {
            loadModule {
                factory { GlobalDependency() }
                viewModel { (state: TestState) -> TestViewModel(state, get()) }
                scope<FragmentActivity> {
                    scoped { ScopedDependency() }
                }
            }
        }

        exception.expectCause<KoinNoFactoryFoundException>()
        MvRxViewModelProvider.get(
            ScopedTestViewModel::class.java,
            ScopedTestState::class.java,
            ActivityViewModelContext(activity, null, customData = defaultScopeProvider),
            ScopedTestViewModel::class.java.name
        )
    }
}