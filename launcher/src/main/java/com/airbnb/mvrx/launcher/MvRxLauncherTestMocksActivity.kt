package com.airbnb.mvrx.launcher

import androidx.appcompat.app.AlertDialog
import com.airbnb.mvrx.BaseMvRxActivity
import com.airbnb.mvrx.mock.MockedViewProvider
import java.util.LinkedList

/**
 * This is given a list of view mocks and automatically opens them all one at a time.
 * Each fragment waits until it successfully loads, then returns so the next one can be loaded.
 * This tests that each mock can be loaded without crashing.
 */
class MvRxLauncherTestMocksActivity : BaseMvRxActivity() {

    private val mockCount = mocksToShow.size

    override fun onResume() {
        super.onResume()

        // This assumes that it isn't rotated or otherwise restarted, and that it will resume when the previously launched screen returns
        val nextMock = mocksToShow.poll() ?: run {
            AlertDialog.Builder(this)
                .setTitle("Complete")
                .setMessage("$mockCount mocks were tested for crashes, and none were found!")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
                .show()
            return
        }

        // TODO Support clicking on views to catch crashes on click
    }

    companion object {
        internal val mocksToShow =
            LinkedList<MockedViewProvider<*>>()
    }
}