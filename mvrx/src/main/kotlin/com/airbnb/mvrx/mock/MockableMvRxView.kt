package com.airbnb.mvrx.mock


import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.mock.MockableMvRxViewMock.Companion.DEFAULT_INITIALIZATION_NAME
import com.airbnb.mvrx.mock.MockableMvRxViewMock.Companion.DEFAULT_STATE_NAME
import com.airbnb.mvrx.mock.MockableMvRxViewMock.Companion.RESTORED_STATE_NAME
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor


interface MockableMvRxView : MvRxView {
    fun mocks(): FragmentMocker<out MockableMvRxView, out Parcelable> = EmptyMocks
}

/**
 * Used with [MockableMvRxView.mocks] for mocking a MvRx fragment that has no view models (eg only static content and fragment arguments).
 *
 * See https://airbnb.quip.com/4S6aAV3uzBRP/Testing-MvRx-Screens
 *
 * @param defaultArgs If your fragment takes arguments you must provide an instance of those arguments here to be used as the default value for your mocks.
 *                      If your fragment has no arguments, pass null.
 */
fun <Frag : MockableMvRxView, Args : Parcelable> Frag.mockNoViewModels(
    defaultArgs: Args?,
    mockBuilder: MockBuilder<Frag, Args>.() -> Unit = {}
): Lazy<MockBuilder<Frag, Args>> = lazy {
    MockBuilder<Frag, Args>(defaultArgs).apply {
        mockBuilder()
        build(this@mockNoViewModels)
    }
}

/**
 * Use this to split a Fragment's mocks into groups. Each group is defined like normal, and this function combines them.
 * This is helpful when mocks need different default states or arguments, so each group can have its own default.
 * Additionally, splitting mocks into groups allows tests to run faster as groups can be tested in parallel.
 *
 * @param mocks Each pair is a String description of the mock group, paired to the mock builder - ie ["Plus" to plusMocks()]. The string description
 * used to create the pair is prepended to the name of each mock in the group, so you can omit a  reference to the group type in the individual mocks.
 */
inline fun <reified Frag : MockableMvRxView> Frag.combineMocks(
    vararg mocks: Pair<String, Lazy<FragmentMocker<Frag, *>>>
): Lazy<FragmentMocker<Frag, *>> = lazy {
    object : FragmentMocker<Frag, Parcelable> {

        init {
            validate(Frag::class.simpleName!!)
        }

        override val mockGroups: List<List<MockableMvRxViewMock<Frag, out Parcelable>>>
            get() {
                println("mocking groups for ${Frag::class.simpleName}")
                return mocks.map { (prefix, lazy) ->
                    lazy.value.fragmentMocks.map {
                        it.copy(name = "$prefix : ${it.name}")
                    }
                }
            }
    }
}

/**
 * Define state values for a [MockableMvRxView] that should be used in tests.
 * This is for use with [MockableMvRxView.mocks] when the fragment has a single view model.
 *
 * In the [mockBuilder] lambda you can use [MockBuilder.args] to define mock arguments that should be used to initialize the fragment and create the
 * initial view model state. Use [SingleViewModelMockBuilder.state] to define complete state objects.
 *
 * See https://airbnb.quip.com/4S6aAV3uzBRP/Testing-MvRx-Screens
 *
 * @param Frag The type of MvRx fragment that is being mocked
 * @param viewModelReference A reference to the view model that will be mocked. Use the "::" operator for this - "MyFragment::myViewModel"
 * @param defaultState An instance of your State that represents the canonical version of your fragment. It will be the basis for your tests.
 * @param defaultArgs If your fragment takes arguments you must provide an instance of those arguments here to be used as the default value for your mocks.
 *                      If your fragment has no arguments, pass null.
 * @see mockTwoViewModels
 */
fun <Frag : MockableMvRxView, Args : Parcelable, S : MvRxState> Frag.mockSingleViewModel(
    viewModelReference: KProperty1<Frag, BaseMvRxViewModel<S>>,
    defaultState: S,
    defaultArgs: Args?,
    mockBuilder: SingleViewModelMockBuilder<Frag, Args, S>.() -> Unit
): Lazy<MockBuilder<Frag, Args>> = lazy {
    SingleViewModelMockBuilder(viewModelReference, defaultState, defaultArgs).apply {
        mockBuilder()
        build(this@mockSingleViewModel)
    }
}

/**
 * Define state values for a [MockableMvRxView] that should be used in tests.
 * This is for use with [MockableMvRxView.mocks] when the fragment has two view models.
 *
 * In the [mockBuilder] lambda you can use [MockBuilder.args] to define mock arguments that should be used to initialize the fragment and create the
 * initial view model state. Use [TwoViewModelMockBuilder.state] to define complete state objects for each view model.
 *
 * See https://airbnb.quip.com/4S6aAV3uzBRP/Testing-MvRx-Screens
 *
 * @param viewModel1Reference A reference to the first view model that will be mocked. Use the "::" operator for this - "MyFragment::myViewModel"
 * @param defaultState1 An canonical instance of your state for the first view model. It will be the basis for your tests.
 * @param viewModel2Reference A reference to the second view model that will be mocked. Use the "::" operator for this - "MyFragment::myViewModel"
 * @param defaultState2 An canonical instance of your state for the second view model. It will be the basis for your tests.
 * @param defaultArgs If your fragment takes arguments you must provide an instance of those arguments here to be used as the default value for your mocks.
 *                      If your fragment has no arguments, pass null.
 */
fun <Frag : MockableMvRxView,
        S1 : MvRxState,
        VM1 : BaseMvRxViewModel<S1>,
        S2 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        Args : Parcelable>
        Frag.mockTwoViewModels(
    viewModel1Reference: KProperty1<Frag, VM1>,
    defaultState1: S1,
    viewModel2Reference: KProperty1<Frag, VM2>,
    defaultState2: S2,
    defaultArgs: Args?,
    mockBuilder: TwoViewModelMockBuilder<Frag, VM1, S1, VM2, S2, Args>.() -> Unit
): Lazy<MockBuilder<Frag, Args>> = lazy {
    TwoViewModelMockBuilder(
        viewModel1Reference,
        defaultState1,
        viewModel2Reference,
        defaultState2,
        defaultArgs
    ).apply {
        mockBuilder()
        build(this@mockTwoViewModels)
    }
}

/**
 * Similar to [mockTwoViewModels], but for the three view model case.
 */
@SuppressWarnings("Detekt.LongParameterList")
fun <Frag : MockableMvRxView,
        S1 : MvRxState,
        VM1 : BaseMvRxViewModel<S1>,
        S2 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        S3 : MvRxState,
        VM3 : BaseMvRxViewModel<S3>,
        Args : Parcelable>
        Frag.mockThreeViewModels(
    viewModel1Reference: KProperty1<Frag, VM1>,
    defaultState1: S1,
    viewModel2Reference: KProperty1<Frag, VM2>,
    defaultState2: S2,
    viewModel3Reference: KProperty1<Frag, VM3>,
    defaultState3: S3,
    defaultArgs: Args?,
    mockBuilder: ThreeViewModelMockBuilder<Frag, VM1, S1, VM2, S2, VM3, S3, Args>.() -> Unit
): Lazy<MockBuilder<Frag, Args>> = lazy {
    ThreeViewModelMockBuilder(
        viewModel1Reference,
        defaultState1,
        viewModel2Reference,
        defaultState2,
        viewModel3Reference,
        defaultState3,
        defaultArgs
    ).apply {
        mockBuilder()
        build(this@mockThreeViewModels)
    }
}

@SuppressWarnings("Detekt.LongParameterList")
fun <Frag : MockableMvRxView,
        S1 : MvRxState,
        VM1 : BaseMvRxViewModel<S1>,
        S2 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        S3 : MvRxState,
        VM3 : BaseMvRxViewModel<S3>,
        S4 : MvRxState,
        VM4 : BaseMvRxViewModel<S4>,
        Args : Parcelable>
        Frag.mockFourViewModels(
    viewModel1Reference: KProperty1<Frag, VM1>,
    defaultState1: S1,
    viewModel2Reference: KProperty1<Frag, VM2>,
    defaultState2: S2,
    viewModel3Reference: KProperty1<Frag, VM3>,
    defaultState3: S3,
    viewModel4Reference: KProperty1<Frag, VM4>,
    defaultState4: S4,
    defaultArgs: Args?,
    mockBuilder: FourViewModelMockBuilder<Frag, VM1, S1, VM2, S2, VM3, S3, VM4, S4, Args>.() -> Unit
): Lazy<MockBuilder<Frag, Args>> = lazy {
    FourViewModelMockBuilder(
        viewModel1Reference,
        defaultState1,
        viewModel2Reference,
        defaultState2,
        viewModel3Reference,
        defaultState3,
        viewModel4Reference,
        defaultState4,
        defaultArgs
    ).apply {
        mockBuilder()
        build(this@mockFourViewModels)
    }
}

data class MockableMvRxViewMock<Frag : MockableMvRxView, Args : Parcelable> internal constructor(
    val name: String,
    /**
     * Returns the arguments that should be used to initialize the fragment. If null, the view models will be
     * initialized purely with the mock states instead.
     */
    val args: Args? = null,
    val states: List<ViewModelState<Frag, *>> = emptyList(),
    /**
     * If true, this mock tests the fragment being created either from arguments or with the viewmodels' default constructor if the fragment
     * doesn't have arguments.
     */
    val forInitialization: Boolean = false,
    val type: Type = Type.Custom
) {

    /**
     * Find a mocked state value for the given view model property.
     *
     * If null is returned it means the initial state should be created through the default mvrx mechanism of args.
     */
    fun stateForViewModelProperty(property: KProperty<*>, existingViewModel: Boolean): MvRxState? {
        if (forInitialization && !existingViewModel) {
            // In the multi viewmodel case, an "existing" view model needs to be mocked with initial state (since it's args
            // would be from a different fragment), but for viewmodels being created in this fragment we can use args to create initial state.
            return null
        }

        // It's possible to have multiple viewmodels of the same type within a fragment. To differentiate them we look
        // at the property names.
        val viewModelToUse = states.firstOrNull { it.viewModelProperty.name == property.name }
        return viewModelToUse?.state
            ?: error("No state found for ViewModel property '${property.name}'. Available view models are ${states.map { it.viewModelProperty.name }}")
    }

    val isDefaultInitialization: Boolean get() = type == Type.DefaultInitialization
    val isDefaultState: Boolean get() = type == Type.DefaultState
    val isForProcessRecreation: Boolean get() = type == Type.ProcessRecreation

    enum class Type {
        DefaultInitialization,
        DefaultState,
        ProcessRecreation,
        Custom
    }

    companion object {
        internal const val DEFAULT_INITIALIZATION_NAME = "Default initialization"
        internal const val DEFAULT_STATE_NAME = "Default state"
        internal const val RESTORED_STATE_NAME = "Default state after process recreation"
    }
}

data class ViewModelState<Frag : MockableMvRxView, S : MvRxState> internal constructor(
    val viewModelProperty: KProperty1<Frag, BaseMvRxViewModel<S>>,
    val state: S
) {
    @SuppressLint("VisibleForTests")
    fun applyState(fragment: Frag) {
        viewModelProperty.get(fragment).freezeStateForTesting(state)
    }
}

class SingleViewModelMockBuilder<Frag : MockableMvRxView, Args : Parcelable, S : MvRxState> internal constructor(
    private val viewModelReference: KProperty1<Frag, BaseMvRxViewModel<S>>,
    private val defaultState: S,
    defaultArgs: Args?
) : MockBuilder<Frag, Args>(defaultArgs, viewModelReference.pairDefault(defaultState)) {

    /**
     * Provide a state object via the lambda for the view model being mocked.
     * The receiver of the lambda is the default state provided in the top level mock method. For simplicity you
     * can modify the receiver directly.
     *
     * @param name Describes the UI the state puts the fragment in. Should be unique.
     * @param args The arguments that should be provided to the fragment.
     *             This is only useful if the fragment accesses arguments directly to get data that is not provided in the view model state.
     *             In other cases it should be omitted. By default the args you set as default in the top level mock method will be used.
     *             The receiver of the lambda is the default args.
     * @param stateBuilder A lambda whose return object is the state for this mock. The lambda receiver is the default state.
     */
    fun state(name: String, args: (Args.() -> Args)? = null, stateBuilder: S.() -> S) {
        addState(
            name = name,
            args = evaluateArgsLambda(args),
            states = listOf(ViewModelState(viewModelReference, defaultState.stateBuilder()))
        )
    }

    /**
     * Helper to mock the loading and failure state of an Async property on your state.
     * Creates two different mocked states stemmed from the given state - one where the async property is set to Loading
     * and one where it is set to Fail.
     */
    fun <T, A : Async<T>> stateForLoadingAndFailure(
        state: S = defaultState,
        asyncPropertyBlock: S.() -> KProperty0<A>
    ) {
        val asyncProperty = state.asyncPropertyBlock()
        // Split "myProperty" to "My property"
        val asyncName = asyncProperty.name.replace(Regex("[A-Z]")) { " ${it.value.toLowerCase()}" }.trim().capitalize()

        state("$asyncName loading") {
            state.setLoading { asyncProperty }
        }

        state("$asyncName failed") {
            state.setNetworkFailure { asyncProperty }
        }
    }
}

private fun <Frag : MockableMvRxView, S : MvRxState, VM : BaseMvRxViewModel<S>> KProperty1<Frag, VM>.pairDefault(
    state: MvRxState
): Pair<KProperty1<Frag, BaseMvRxViewModel<MvRxState>>, MvRxState> {
    @Suppress("UNCHECKED_CAST")
    return (this to state) as Pair<KProperty1<Frag, BaseMvRxViewModel<MvRxState>>, MvRxState>
}

class TwoViewModelMockBuilder<
        Frag : MockableMvRxView,
        VM1 : BaseMvRxViewModel<S1>,
        S1 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        S2 : MvRxState,
        Args : Parcelable>
internal constructor(
    val vm1: KProperty1<Frag, VM1>,
    val defaultState1: S1,
    val vm2: KProperty1<Frag, VM2>,
    val defaultState2: S2,
    defaultArgs: Args?
) : MockBuilder<Frag, Args>(defaultArgs, vm1.pairDefault(defaultState1), vm2.pairDefault(defaultState2)) {

    /**
     * Provide state objects for each view model in the fragment.
     *
     * @param name Describes the UI these states put the fragment in. Should be unique.
     * @param args The arguments that should be provided to the fragment.
     *             This is only useful if the fragment accesses arguments directly to get data that is not provided in the view model state.
     *             In other cases it should be omitted. By default the args you set as default in the top level mock method will be used.
     *             The receiver of the lambda is the default args.
     * @param statesBuilder A lambda that is used to define state objects for each view model.
     */
    fun state(
        name: String,
        args: (Args.() -> Args)? = null,
        statesBuilder: TwoStatesBuilder<Frag, S1, VM1, S2, VM2>.() -> Unit
    ) {
        addState(
            name, evaluateArgsLambda(args), TwoStatesBuilder(
                vm1,
                defaultState1,
                vm2,
                defaultState2
            ).apply(statesBuilder).states
        )
    }

    /**
     * Helper to mock the loading and failure state of an Async property on your state in the first view model.
     * Creates two different mocked states stemmed from the given state - one where the async property is set to Loading
     * and one where it is set to Fail.
     */
    fun <T, A : Async<T>> viewModel1StateForLoadingAndFailure(
        state: S1 = defaultState1,
        asyncPropertyBlock: S1.() -> KProperty0<A>
    ) {
        val asyncProperty = state.asyncPropertyBlock()
        // Split "myProperty" to "My property"
        val asyncName = asyncProperty.name.replace(Regex("[A-Z]")) { " ${it.value.toLowerCase()}" }.trim().capitalize()

        state("$asyncName loading") {
            viewModel1 {
                state.setLoading { asyncProperty }
            }
        }

        state("$asyncName failed") {
            viewModel1 {
                state.setNetworkFailure { asyncProperty }
            }
        }
    }

    /**
     * Helper to mock the loading and failure state of an Async property on your state in the second view model.
     * Creates two different mocked states stemmed from the given state - one where the async property is set to Loading
     * and one where it is set to Fail.
     */
    fun <T, A : Async<T>> viewModel2StateForLoadingAndFailure(
        state: S2 = defaultState2,
        asyncPropertyBlock: S2.() -> KProperty0<A>
    ) {
        val asyncProperty = state.asyncPropertyBlock()
        // Split "myProperty" to "My property"
        val asyncName = asyncProperty.name.replace(Regex("[A-Z]")) { " ${it.value.toLowerCase()}" }.trim().capitalize()

        state("$asyncName loading") {
            viewModel2 {
                state.setLoading { asyncProperty }
            }
        }

        state("$asyncName failed") {
            viewModel2 {
                state.setNetworkFailure { asyncProperty }
            }
        }
    }
}

/**
 * Helper to provide mock state objects for multiple view models.
 */
open class TwoStatesBuilder<
        Frag : MockableMvRxView,
        S1 : MvRxState,
        VM1 : BaseMvRxViewModel<S1>,
        S2 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>>
internal constructor(
    val vm1: KProperty1<Frag, VM1>,
    val defaultState1: S1,
    val vm2: KProperty1<Frag, VM2>,
    val defaultState2: S2
) {
    private val stateMap = mutableMapOf<KProperty1<Frag, BaseMvRxViewModel<MvRxState>>, MvRxState>()

    internal val states: List<ViewModelState<Frag, *>>
        get() = stateMap.map {
            ViewModelState(
                it.key,
                it.value
            )
        }

    protected infix fun <VM : BaseMvRxViewModel<S>, S : MvRxState> KProperty1<Frag, VM>.setStateTo(state: S) {
        @Suppress("UNCHECKED_CAST")
        stateMap[this as KProperty1<Frag, BaseMvRxViewModel<MvRxState>>] = state
    }

    init {
        vm1 setStateTo defaultState1
        vm2 setStateTo defaultState2
    }

    /**
     * Define a state to be used when mocking your first view model (as defined in the top level mock method).
     * If this method isn't called, your default state will be used automatically.
     * For convenience, the receiver of the lambda is the default state.
     */
    fun viewModel1(stateBuilder: S1.() -> S1) {
        vm1 setStateTo defaultState1.stateBuilder()
    }

    /**
     * Define a state to be used when mocking your second view model (as defined in the top level mock method).
     * If this method isn't called, your default state will be used automatically.
     * For convenience, the receiver of the lambda is the default state.
     */
    fun viewModel2(stateBuilder: S2.() -> S2) {
        vm2 setStateTo defaultState2.stateBuilder()
    }
}

class ThreeViewModelMockBuilder<
        Frag : MockableMvRxView,
        VM1 : BaseMvRxViewModel<S1>,
        S1 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        S2 : MvRxState,
        VM3 : BaseMvRxViewModel<S3>,
        S3 : MvRxState,
        Args : Parcelable>
internal constructor(
    val vm1: KProperty1<Frag, VM1>,
    val defaultState1: S1,
    val vm2: KProperty1<Frag, VM2>,
    val defaultState2: S2,
    val vm3: KProperty1<Frag, VM3>,
    val defaultState3: S3,
    defaultArgs: Args?
) : MockBuilder<Frag, Args>(
    defaultArgs,
    vm1.pairDefault(defaultState1),
    vm2.pairDefault(defaultState2),
    vm3.pairDefault(defaultState3)
) {

    /**
     * Provide state objects for each view model in the fragment.
     *
     * @param name Describes the UI these states put the fragment in. Should be unique.
     * @param args The arguments that should be provided to the fragment.
     *             This is only used if the fragment accesses arguments directly to get data that is not provided in the view model state.
     *             In other cases it should be omitted. This must be provided if the fragment accesses args directly.
     * @param statesBuilder A lambda that is used to define state objects for each view model. See [TwoStatesBuilder.invoke]
     */
    fun state(
        name: String,
        args: (Args.() -> Args)? = null,
        statesBuilder: ThreeStatesBuilder<Frag, S1, VM1, S2, VM2, S3, VM3>.() -> Unit
    ) {
        addState(
            name, evaluateArgsLambda(args), ThreeStatesBuilder(
                vm1,
                defaultState1,
                vm2,
                defaultState2,
                vm3,
                defaultState3
            ).apply(statesBuilder).states
        )
    }
}

open class ThreeStatesBuilder<
        Frag : MockableMvRxView,
        S1 : MvRxState,
        VM1 : BaseMvRxViewModel<S1>,
        S2 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        S3 : MvRxState,
        VM3 : BaseMvRxViewModel<S3>>
internal constructor(
    vm1: KProperty1<Frag, VM1>,
    defaultState1: S1,
    vm2: KProperty1<Frag, VM2>,
    defaultState2: S2,
    val vm3: KProperty1<Frag, VM3>,
    val defaultState3: S3
) : TwoStatesBuilder<Frag, S1, VM1, S2, VM2>(vm1, defaultState1, vm2, defaultState2) {

    init {
        vm3 setStateTo defaultState3
    }

    /**
     * Define a state to be used when mocking your third view model (as defined in the top level mock method).
     * If this method isn't called, your default state will be used automatically.
     * For convenience, the receiver of the lambda is the default state.
     */
    fun viewModel3(stateBuilder: S3.() -> S3) {
        vm3 setStateTo defaultState3.stateBuilder()
    }
}

class FourViewModelMockBuilder<
        Frag : MockableMvRxView,
        VM1 : BaseMvRxViewModel<S1>,
        S1 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        S2 : MvRxState,
        VM3 : BaseMvRxViewModel<S3>,
        S3 : MvRxState,
        VM4 : BaseMvRxViewModel<S4>,
        S4 : MvRxState,
        Args : Parcelable>
internal constructor(
    val vm1: KProperty1<Frag, VM1>,
    val defaultState1: S1,
    val vm2: KProperty1<Frag, VM2>,
    val defaultState2: S2,
    val vm3: KProperty1<Frag, VM3>,
    val defaultState3: S3,
    val vm4: KProperty1<Frag, VM4>,
    val defaultState4: S4,
    defaultArgs: Args?
) : MockBuilder<Frag, Args>(
    defaultArgs,
    vm1.pairDefault(defaultState1),
    vm2.pairDefault(defaultState2),
    vm3.pairDefault(defaultState3),
    vm4.pairDefault(defaultState4)
) {

    /**
     * Provide state objects for each view model in the fragment.
     *
     * @param name Describes the UI these states put the fragment in. Should be unique.
     * @param args The arguments that should be provided to the fragment.
     *             This is only used if the fragment accesses arguments directly to get data that is not provided in the view model state.
     *             In other cases it should be omitted. This must be provided if the fragment accesses args directly.
     * @param statesBuilder A lambda that is used to define state objects for each view model. See [TwoStatesBuilder.invoke]
     */
    fun state(
        name: String,
        args: (Args.() -> Args)? = null,
        statesBuilder: FourStatesBuilder<Frag, S1, VM1, S2, VM2, S3, VM3, S4, VM4>.() -> Unit
    ) {
        addState(
            name,
            evaluateArgsLambda(args),
            FourStatesBuilder(
                vm1,
                defaultState1,
                vm2,
                defaultState2,
                vm3,
                defaultState3,
                vm4,
                defaultState4
            ).apply(statesBuilder).states
        )
    }
}

class FourStatesBuilder<
        Frag : MockableMvRxView,
        S1 : MvRxState,
        VM1 : BaseMvRxViewModel<S1>,
        S2 : MvRxState,
        VM2 : BaseMvRxViewModel<S2>,
        S3 : MvRxState,
        VM3 : BaseMvRxViewModel<S3>,
        S4 : MvRxState,
        VM4 : BaseMvRxViewModel<S4>>
internal constructor(
    vm1: KProperty1<Frag, VM1>,
    defaultState1: S1,
    vm2: KProperty1<Frag, VM2>,
    defaultState2: S2,
    vm3: KProperty1<Frag, VM3>,
    defaultState3: S3,
    val vm4: KProperty1<Frag, VM4>,
    val defaultState4: S4
) : ThreeStatesBuilder<Frag, S1, VM1, S2, VM2, S3, VM3>(vm1, defaultState1, vm2, defaultState2, vm3, defaultState3) {

    init {
        vm4 setStateTo defaultState4
    }

    /**
     * Define a state to be used when mocking your fourth view model (as defined in the top level mock method).
     * If this method isn't called, your default state will be used automatically.
     * For convenience, the receiver of the lambda is the default state.
     */
    fun viewModel4(stateBuilder: S4.() -> S4) {
        vm4 setStateTo defaultState4.stateBuilder()
    }
}

/**
 * This placeholder can be used as a NO-OP implementation of [MockableMvRxView.mocks].
 */
object EmptyMocks : FragmentMocker<MockableMvRxView, Nothing> {
    override val fragmentMocks: List<MockableMvRxViewMock<MockableMvRxView, out Nothing>> = emptyList()
    override val mockGroups: List<List<MockableMvRxViewMock<MockableMvRxView, out Nothing>>> = emptyList()
}

interface FragmentMocker<Frag : MockableMvRxView, Args : Parcelable> {
    /** At least one of fragmentMocks or mockGroups must be implemented to avoid a loop. */
    val fragmentMocks: List<MockableMvRxViewMock<Frag, out Args>> get() = mockGroups.flatten()

    /**
     * Groups allow splitting up a fragment with lots of mocks so they can be run in separate tests (better parallelization),
     * with separate default args and states.
     */
    val mockGroups: List<List<MockableMvRxViewMock<Frag, out Args>>> get() = listOf(fragmentMocks)

    fun validate(fragmentName: String) {
        // TODO eli_hart: 2018-11-06 Gather all validation errors in one exception instead of failing early, so that you don't have to do multiple test runs to catch multiple issues

        val errorIntro = "Invalid mocks defined for $fragmentName. "
        val (fragmentMocksWithArgsOnly, fragmentMocksWithState) = fragmentMocks.partition { it.states.isEmpty() }

        fun List<MockableMvRxViewMock<Frag, *>>.validateUniqueNames() {
            val nameCounts = groupingBy { it.name }.eachCount()
            nameCounts.forEach { (name, count) ->
                require(count == 1) { "$errorIntro '$name' was used multiple times. MvRx mock state and argument names must be unique." }
            }
        }

        // We allow args and states to share names, such as "default", since they are different use cases.
        fragmentMocksWithArgsOnly.validateUniqueNames()
        fragmentMocksWithState.validateUniqueNames()
    }
}

open class MockBuilder<Frag : MockableMvRxView, Args : Parcelable> internal constructor(
    internal val defaultArgs: Args?,
    vararg defaultStatePairs: Pair<KProperty1<Frag, BaseMvRxViewModel<MvRxState>>, MvRxState>
) : FragmentMocker<Frag, Args>, DataClassSetDsl {

    internal val defaultStates = defaultStatePairs.map { ViewModelState(it.first, it.second) }

    @VisibleForTesting
    override val fragmentMocks = mutableListOf<MockableMvRxViewMock<Frag, Args>>()

    init {
        val viewModelProperties = defaultStates.map { it.viewModelProperty }
        require(viewModelProperties.distinct().size == defaultStates.size) {
            "Duplicate viewmodels were passed to the mock method - ${viewModelProperties.map { it.name }}"
        }

        // Even if args are null this is useful to add because it tests the code flow where initial state
        // is created from it's defaults.
        // This isn't necessary in the case of "existingViewModel" tests, but we can't know whether that's the case
        // at this point, so we need to add it anyway.
        addState(
            name = DEFAULT_INITIALIZATION_NAME,
            args = defaultArgs,
            states = defaultStates,
            forInitialization = true,
            type = MockableMvRxViewMock.Type.DefaultInitialization
        )

        if (defaultStates.isNotEmpty()) {
            addState(
                name = DEFAULT_STATE_NAME,
                args = defaultArgs,
                states = defaultStates,
                type = MockableMvRxViewMock.Type.DefaultState
            )
            addState(
                name = RESTORED_STATE_NAME,
                args = defaultArgs,
                states = defaultStates.map { it.copy(state = it.state.toRestoredState()) },
                type = MockableMvRxViewMock.Type.ProcessRecreation
            )
        }
    }

    /**
     * Provide an instance of arguments to use when initializing a fragment and creating initial view model state.
     * It is only valid to call this if you defined default arguments in the top level mock method.
     * For convenience, the receiver of the lambda is the default arguments.
     * @param name Describes what state these arguments put the fragment in. Should be unique.
     */
    fun args(name: String, builder: Args.() -> Args) {
        addState(name, evaluateArgsLambda(builder), defaultStates, forInitialization = true)
    }

    internal fun evaluateArgsLambda(builder: (Args.() -> Args)?): Args? {
        if (builder == null) {
            return defaultArgs
        }

        requireNotNull(defaultArgs) { "Args cannot be provided unless you have set a default value for them in the top level mock method" }

        return builder.invoke(defaultArgs)
    }

    protected fun addState(
        name: String,
        args: Args? = defaultArgs,
        states: List<ViewModelState<Frag, *>>,
        forInitialization: Boolean = false,
        type: MockableMvRxViewMock.Type = MockableMvRxViewMock.Type.Custom
    ) {
        fragmentMocks.add(
            MockableMvRxViewMock(
                name = name,
                args = args,
                states = states,
                forInitialization = forInitialization,
                type = type
            )
        )
    }

    internal fun build(fragment: Frag) {
        validate(fragment::class.java.simpleName)
    }

    private fun <S : MvRxState> S.toRestoredState(): S {
        val klass = this::class

        /** Filter out params that don't have an associated @PersistState prop.
         * Map the parameter name to the current value of its associated property
         * Reduce the @PersistState parameters into a bundle mapping the parameter name to the property value.
         */
        return klass.primaryConstructor!!
            .parameters
            .filter {
                it.annotations.any { it.annotationClass == PersistState::class } || !it.isOptional
            }
            .map { param ->
                val prop = klass.declaredMemberProperties.single { it.name == param.name }
                @Suppress("UNCHECKED_CAST")
                val value = (prop as? KProperty1<S, Any?>)?.get(this)
                param to value
            }
            .let { pairs ->
                (klass.primaryConstructor as KFunction<S>).callBy(pairs.toMap())
            }
    }
}


