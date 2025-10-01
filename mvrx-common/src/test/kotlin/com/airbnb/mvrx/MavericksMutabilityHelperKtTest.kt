package com.airbnb.mvrx

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MavericksMutabilityHelperKtTest {

    @Test
    fun isData() {
        assertTrue(TestDataClass::class.java.isData)
        assertFalse(String::class.java.isData)
    }

    @Test
    fun isDataWithValueClass() {
        assertTrue(TestDataClassWithValueClass::class.java.isData)
    }

    @Test
    fun isDataWithValueClassAsFirstParameter() {
        // When a value class is the first parameter, component1 is mangled
        assertTrue(TestDataClassWithValueClassFirst::class.java.isData)
    }

    @Test
    fun assertImmutabilityWithValueClass() {
        // Test that assertMavericksDataClassImmutability works with value classes
        assertMavericksDataClassImmutability(TestDataClassWithValueClass::class)
        assertMavericksDataClassImmutability(TestDataClassWithValueClassFirst::class)
    }

    @Test
    fun isDataWithOnlyValueClass() {
        assertTrue(TestDataClassWithOnlyValueClass::class.java.isData)
    }

    @Test
    fun isDataWithMultipleValueClasses() {
        assertTrue(TestDataClassWithMultipleValueClasses::class.java.isData)
    }

    @JvmInline
    value class TestValueClass(val value: Int)

    data class TestDataClass(
        internal val foo: Int
    )

    data class TestDataClassWithValueClass(
        val foo: Int,
        val valueClass: TestValueClass
    )

    data class TestDataClassWithValueClassFirst(
        val valueClass: TestValueClass,
        val foo: Int
    )

    data class TestDataClassWithOnlyValueClass(
        val valueClass: TestValueClass
    )

    data class TestDataClassWithMultipleValueClasses(
        val valueClass1: TestValueClass,
        val valueClass2: TestValueClass
    )
}
