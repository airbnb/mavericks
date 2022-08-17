package com.airbnb.mvrx.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.launcher.MavericksLauncherTestMocksActivity.Companion.provideIntentToTestMock
import com.airbnb.mvrx.mocking.MockedViewProvider
import java.util.LinkedList

/**
 * This is given a list of view mocks and automatically opens them all one at a time.
 * Each fragment waits until it successfully loads, then returns so the next one can be loaded.
 * This tests that each mock can be loaded without crashing.
 *
 * Override [provideIntentToTestMock] in order to customize what activity is used to test each
 * mock.
 */
class MavericksLauncherTestMocksActivity : FragmentActivity() {

    private val mockCount = mocksToShow.size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            testNextMock()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_TEST_FINISHED) {
            testNextMock()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun testNextMock() {
        // This assumes that it isn't rotated or otherwise restarted, and that it will resume when the previously launched screen returns
        val nextMock = mocksToShow.poll() ?: run {
            AlertDialog.Builder(this)
                .setTitle("Complete")
                .setMessage("$mockCount mocks were tested for crashes, and no crashes were found!")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
                .show()
            return
        }

        val intent = provideIntentToTestMock(this, nextMock)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_CODE_TEST_FINISHED)

        // TODO Support clicking on views to catch crashes on click
    }

    companion object {
        internal val mocksToShow = LinkedList<MockedViewProvider<*>>()

        private const val REQUEST_CODE_TEST_FINISHED = 2

        /**
         * Override this in order to customize what activity is used to test each
         * mock.
         */
        var provideIntentToTestMock: (Context, MockedViewProvider<*>) -> Intent = { context, mock ->
            MavericksLauncherMockActivity.intent(context, mock, finishAfterLaunch = true)
        }
    }
}
