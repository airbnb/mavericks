package com.airbnb.mvrx

import org.robolectric.RobolectricTestRunner

class MvRxTestRunner(testClass: Class<*>) : RobolectricTestRunner(testClass) {
    companion object {
        const val MANIFEST_PATH = "../lib.mvrx.base/src/main/AndroidManifest.xml"
        const val PACKAGE_NAME = "com.airbnb.mvrx"
    }
}
