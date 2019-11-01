package com.airbnb.mvrx.mock

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxView
import kotlin.system.measureTimeMillis

/**
 * See [getMockVariants]
 *
 * Provides mocks for the view specified by the fully qualified name of its class..
 *
 * @param viewClassName The fully qualified name of the view's class.
 * @param viewProvider Creates an instance of the view from the view class. The view should be created
 * with the given arguments bundle if it exists. The bundle contains parcelable arguments under the
 * [MvRx.KEY_ARG] key.
 */
fun getMockVariants(
    viewClassName: String,
    viewProvider: (viewClass: Class<MvRxView>, argumentsBundle: Bundle?) -> MvRxView = { viewClass, argumentsBundle ->
        @Suppress("UNCHECKED_CAST")
        val view = viewClass.newInstance() as MvRxView
        if (view is Fragment) view.arguments = argumentsBundle
        view
    },
    emptyMockPlaceholder: Pair<String, MvRxViewMocks<MvRxView, Nothing>> = Pair(
        EmptyMocks::class.java.simpleName,
        EmptyMocks
    ),
    mockGroupIndex: Int? = null
): List<MockedViewProvider<MvRxView>>? {
    @Suppress("UNCHECKED_CAST")
    return getMockVariants(
        viewClass = Class.forName(viewClassName) as Class<MvRxView>,
        viewProvider = viewProvider,
        emptyMockPlaceholder = emptyMockPlaceholder,
        mockGroupIndex = mockGroupIndex
    )
}

/**
 * See [getMockVariants]
 *
 * Provides mocks for the view specified by the given class.
 *
 * @param viewClass The Java class representing the [MvRxView]
 * @param viewProvider Creates an instance of the view from the view class. The view should be created
 * with the given arguments bundle if it exists. The bundle contains parcelable arguments under the
 * [MvRx.KEY_ARG] key.
 */
fun getMockVariants(
    viewClass: Class<MvRxView>,
    viewProvider: (viewClass: Class<MvRxView>, argumentsBundle: Bundle?) -> MvRxView = { viewClass, argumentsBundle ->
        @Suppress("UNCHECKED_CAST")
        val view = viewClass.newInstance() as MvRxView
        if (view is Fragment) view.arguments = argumentsBundle
        view
    },
    emptyMockPlaceholder: Pair<String, MvRxViewMocks<MvRxView, Nothing>> = Pair(
        EmptyMocks::class.java.simpleName,
        EmptyMocks
    ),
    mockGroupIndex: Int? = null
): List<MockedViewProvider<MvRxView>>? {
    return getMockVariants<MvRxView, Nothing>(
        viewProvider = { _, argumentsBundle ->
            viewProvider(viewClass, argumentsBundle)
        },
        emptyMockPlaceholder = emptyMockPlaceholder,
        mockGroupIndex = mockGroupIndex
    )
}

/**
 * Get all of the mocks declared for a [MvRxView]. The returned list of [MockedViewProvider]
 * has one entry representing each mock in the view, which can be used to create a view mocked
 * with that state and arguments.
 *
 * @param viewProvider Return an instance of the [MvRxView] that is being mocked. It should be
 * created with the provided arguments if they exist. Both the arguments and argumentsBundle
 * parameters in the lambda represent the same arguments, they are just provided in multiple
 * forms so you can use the most convenient. The Bundle just contains the Parcelable arguments
 * under the [MvRx.KEY_ARG] key.
 *
 * @param emptyMockPlaceholder If you use a custom object to represent that a [MvRxView] legitimately
 * has no mocks then specify it here. Otherwise any view with empty mocks will throw an error.
 *
 * @param mockGroupIndex If the mocks are declared with [combineMocks] then if an index is passed
 * only the mocks in that group index will be returned. Will throw an error if this is not a valid
 * group index for this view's mocks.
 */
fun <V : MvRxView, A : Parcelable> getMockVariants(
    viewProvider: (arguments: A?, argumentsBundle: Bundle?) -> V,
    emptyMockPlaceholder: Pair<String, MvRxViewMocks<MvRxView, Nothing>> = Pair(
        EmptyMocks::class.java.simpleName,
        EmptyMocks
    ),
    mockGroupIndex: Int? = null
): List<MockedViewProvider<V>>? {
    val view = viewProvider(null, null)
    // This needs to be the FQN to completely define the view and make it creatable from reflection
    val viewName = view.javaClass.canonicalName

    lateinit var mocks: List<MvRxMock<out MvRxView, out Parcelable>>
    val elapsedMs: Long = measureTimeMillis {
        val mockData =
            MvRxViewMocks.getFrom(view).takeIf { it !== emptyMockPlaceholder.second } ?: return null

        mocks = if (mockGroupIndex != null) {
            mockData.mockGroups.getOrNull(mockGroupIndex)
                ?: error("Could not get mock group at index $mockGroupIndex for $viewName")
        } else {
            mockData.mocks
        }
    }

    println("Created mocks for $viewName in $elapsedMs ms")

    require(mocks.isNotEmpty()) {
        "No mocks provided for $viewName. Use the placeholder '${emptyMockPlaceholder.first}' to skip adding mocks"
    }

    return mocks.map { mvRxFragmentMock ->
        @Suppress("UNCHECKED_CAST")
        val mockInfo = mvRxFragmentMock as MvRxMock<V, A>

        MockedViewProvider(
            viewName = viewName,
            createView = { mockBehavior ->
                val configProvider = MvRx.viewModelConfigProvider

                // Test argument serialization/deserialization

                val arguments = mockInfo.args
                val bundle = if (arguments != null) argumentsBundle(arguments, viewName) else null

                configProvider.withMockBehavior(mockBehavior) {
                    viewProvider(arguments, bundle)
                }.let { view ->
                    // Set the view to be initialized with the mocked state when its viewmodels are created
                    MvRx.mockStateHolder.setMock(view, mockInfo)
                    MockedView(
                        viewInstance = view,
                        viewName = viewName,
                        mockData = mockInfo,
                        cleanupMockState = { MvRx.mockStateHolder.clearMock(view) }
                    )
                }
            },
            mock = mockInfo
        )
    }

}

private fun argumentsBundle(arguments: Parcelable, viewName: String): Bundle {
    @Suppress("Detekt.TooGenericExceptionCaught")
    return try {
        Bundle().apply {
            putParcelable(MvRx.KEY_ARG, arguments)
        }.makeClone()
    } catch (e: Throwable) {
        throw AssertionError(
            "The arguments class ${arguments::class.simpleName} for view " +
                    "$viewName failed to be parceled. Make sure it is a valid Parcelable " +
                    "and all properties on it are valid Parcelables or Serializables.",
            e
        )
    }
}

private fun <T : Parcelable> T.makeClone(): T {
    val p = Parcel.obtain()
    p.writeValue(this)
    p.setDataPosition(0)
    @Suppress("UNCHECKED_CAST")
    val clone = p.readValue(MockedView::class.java.classLoader) as T
    p.recycle()
    return clone
}

data class MockedViewProvider<V : MvRxView>(
    val viewName: String,
    val createView: (MockBehavior) -> MockedView<V>,
    val mock: MvRxMock<V, *>
)

/**
 * @property cleanupMockState Call this when the view is done initializing its viewmodels, so that the global mock state can be cleared.
 */
class MockedView<V : MvRxView>(
    val viewInstance: V,
    val viewName: String,
    val mockData: MvRxMock<V, *>,
    val cleanupMockState: () -> Unit
)