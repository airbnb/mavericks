package com.airbnb.mvrx

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.Semaphore
import kotlin.properties.Delegates


class ViewViewModelTest : BaseTest() {

    data class ViewState(val title: String) : MvRxState
    class ViewViewModel(state: ViewState) : TestMvRxViewModel<ViewState>(state) {

        fun setTitle(title: String) = setState { copy(title = title) }

        companion object : MvRxViewModelFactory<ViewViewModel, ViewState> {
            override fun initialState(viewModelContext: ViewModelContext): ViewState? {
                return ViewState(title = "Key: ${viewModelContext.key}")
            }
        }
    }

    class TitleView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : TextView(context, attrs, defStyleAttr), StatefulView {
        val viewModel: ViewViewModel by viewViewModel(keyFactory = { key })
        var invalidateCount = 0
        var key by Delegates.observable("key_0") { _, _, _ -> postInvalidate() }

        override fun onInvalidate() = withState(viewModel) { state ->
            invalidateCount++
            text = state.title
        }
    }

    class ViewFragment : BaseMvRxFragment() {

        lateinit var titleView: TitleView

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            titleView = TitleView(requireContext())
            return FrameLayout(requireContext()).apply { addView(titleView) }
        }

        override fun invalidate() {

        }
    }

    @Test
    fun testBasicViewViewModel() {
        val (_, fragment) = createFragment<ViewFragment, TestActivity>(containerId = CONTAINER_ID)
        assertEquals("Key: key_0", fragment.titleView.text)
    }

    @Test
    fun testCanChangeKeys() {
        val (_, fragment) = createFragment<ViewFragment, TestActivity>(containerId = CONTAINER_ID)
        val firstViewModel = fragment.titleView.viewModel
        assertEquals(2, fragment.titleView.invalidateCount)
        fragment.titleView.key = "key_1"
        assertEquals(4, fragment.titleView.invalidateCount)
        assertEquals("Key: key_1", fragment.titleView.text)
        val secondViewModel = fragment.titleView.viewModel
        assertNotEquals(firstViewModel, secondViewModel)
    }

    @Test
    fun testOnlySubscribesToNewViewModelWhenKeyChanges() {
        val (_, fragment) = createFragment<ViewFragment, TestActivity>(containerId = CONTAINER_ID)
        val firstViewModel = fragment.titleView.viewModel
        assertEquals(2, fragment.titleView.invalidateCount)
        fragment.titleView.key = "key_1"
        val secondViewModel = fragment.titleView.viewModel

        // Ensure the UI updates when the key changes.
        assertEquals("Key: key_1", fragment.titleView.text)
        assertEquals(4, fragment.titleView.invalidateCount)

        firstViewModel.setTitle("Update Original ViewModel")
        assertEquals(4, fragment.titleView.invalidateCount)

        secondViewModel.setTitle("Update New ViewModel")
        assertEquals(5, fragment.titleView.invalidateCount)
    }

    @Test
    fun testInvalidatesAtTheRightTime() {
        val (_, fragment) = createFragment<ViewFragment, TestActivity>(containerId = CONTAINER_ID)
        assertEquals(2, fragment.titleView.invalidateCount)
        (fragment.view as ViewGroup).removeView(fragment.titleView)
        fragment.titleView.viewModel.setTitle("Update 1")
        fragment.titleView.viewModel.setTitle("Update 2")
        assertEquals(2, fragment.titleView.invalidateCount)
        (fragment.view as ViewGroup).addView(fragment.titleView)
        assertEquals(4, fragment.titleView.invalidateCount)
        fragment.titleView.viewModel.setTitle("Update 3")
        assertEquals(5, fragment.titleView.invalidateCount)

    }

    @Test
    fun testUpdatesWithState() {
        val (_, fragment) = createFragment<ViewFragment, TestActivity>(containerId = CONTAINER_ID)
        assertEquals(2, fragment.titleView.invalidateCount)
        fragment.titleView.viewModel.setTitle("Update!")
        assertEquals("Update!", fragment.titleView.text)
        assertEquals(3, fragment.titleView.invalidateCount)
        fragment.titleView.viewModel.setTitle("Update2!")
        assertEquals("Update2!", fragment.titleView.text)
        assertEquals(4, fragment.titleView.invalidateCount)
    }

    @Test
    fun testOnlyInvalidesOnceWhenSwitchingViewModelKeys() {
        val (controller, fragment) = createFragment<ViewFragment, TestActivity>(containerId = CONTAINER_ID)
        assertEquals(2, fragment.titleView.invalidateCount)
        fragment.titleView.key = "key_1"
        val semaphore = Semaphore(0)
        controller.get().runOnUiThread { semaphore.release() }
        semaphore.acquire()
        assertEquals("Key: key_1", fragment.titleView.text)
        assertEquals(3, fragment.titleView.invalidateCount)
    }
}