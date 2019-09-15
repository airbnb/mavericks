package com.airbnb.mvrx.mock

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.airbnb.mvrx.MvRx
import kotlin.system.measureTimeMillis

fun <V : MockableMvRxView, A : Parcelable> getMockVariants(
    viewProvider: (arguments: A?, argumentsBundle: Bundle?) -> V,
    emptyMockPlaceholder: Pair<String, MvRxViewMocks<MockableMvRxView, Nothing>> = Pair(
        EmptyMocks::class.java.simpleName,
        EmptyMocks
    ),
    mockGroupIndex: Int? = null
): List<MockedViewProvider<V>>? {
    val view = viewProvider(null, null)
    val viewName = view.javaClass.simpleName

    // TODO
//    testUninitializedAccess(fragment)

    lateinit var mocks: List<MvRxMock<out MockableMvRxView, out Parcelable>>
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
                        mockData = mockInfo
                    ) { MvRx.mockStateHolder.clearMock(view) }
                }
            },
            mockData = mockInfo
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

class MockedViewProvider<V : MockableMvRxView>(
    val viewName: String,
    val createView: (MockBehavior) -> MockedView<V>,
    val mockData: MvRxMock<V, *>
)

/**
 * @property cleanupMockState Call this when the view is done initializing its viewmodels, so that the global mock state can be cleared.
 */
class MockedView<V : MockableMvRxView>(
    val viewInstance: V,
    val viewName: String,
    val mockData: MvRxMock<V, *>,
    val cleanupMockState: () -> Unit
)