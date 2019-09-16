package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseTest
import org.junit.Assert.*
import org.junit.Test

class AutoValueTypePrinterTest : BaseTest() {

    @Test
    fun typePrinterAcceptsAutoValueType() {
        val obj = AutoValueClass.builder().setName("foo").setNumberOfLegs(1).build()
        assertTrue(AutoValueTypePrinter().acceptsObject(obj))
    }

    @Test
    fun typePrinterGeneratesCode() {
        val obj = AutoValueClass.builder().setName("foo").setNumberOfLegs(1).build()
        val constructorCode = ConstructorCode(
            obj,
            customTypePrinters = listOf(AutoValueTypePrinter())
        )

        assertEquals(
            "val mockAutoValue_AutoValueClass by lazy { AutoValueClass.builder()\n" +
                    ".setName(\"foo\")\n" +
                    ".setNumberOfLegs(1)\n" +
                    ".build() }", constructorCode.lazyPropertyToCreateObject
        )
    }

    @Test
    fun typePrinterUsesCorrectImport() {
        val obj = AutoValueClass.builder().setName("foo").setNumberOfLegs(1).build()
        val constructorCode = ConstructorCode(
            obj,
            customTypePrinters = listOf(AutoValueTypePrinter())
        )

        assertEquals(
            listOf("com.airbnb.mvrx.mock.AutoValueClass", "kotlin.Int", "kotlin.String"),
            constructorCode.imports
        )
    }
}
