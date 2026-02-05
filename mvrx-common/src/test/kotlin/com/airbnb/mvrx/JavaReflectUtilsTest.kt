package com.airbnb.mvrx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for the Java reflection utilities in MavericksMutabilityHelper.
 * These tests don't require Robolectric/Android and directly test the
 * reflection-based data class utilities.
 */
class JavaReflectUtilsTest {

    // Test data classes
    data class SimpleDataClass(
        val name: String = "default",
        val count: Int = 0,
        val enabled: Boolean = false
    )

    data class NestedDataClass(
        val id: Long = 0,
        val simple: SimpleDataClass = SimpleDataClass(),
        val nullableValue: String? = null
    )

    data class DeeplyNestedDataClass(
        val level1: NestedDataClass = NestedDataClass()
    )

    // Non-data class for negative testing
    class RegularClass(val value: String)

    // ==================== isData Tests ====================

    @Test
    fun `isData returns true for simple data class`() {
        assertTrue(SimpleDataClass::class.java.isData)
    }

    @Test
    fun `isData returns true for nested data class`() {
        assertTrue(NestedDataClass::class.java.isData)
    }

    @Test
    fun `isData returns false for regular class`() {
        assertFalse(RegularClass::class.java.isData)
    }

    @Test
    fun `isData returns false for Java class`() {
        assertFalse(String::class.java.isData)
    }

    // ==================== getPropertyValue Tests ====================

    @Test
    fun `getPropertyValue gets string property`() {
        val instance = SimpleDataClass(name = "test")
        assertEquals("test", instance.getPropertyValue("name"))
    }

    @Test
    fun `getPropertyValue gets int property`() {
        val instance = SimpleDataClass(count = 42)
        assertEquals(42, instance.getPropertyValue("count"))
    }

    @Test
    fun `getPropertyValue gets boolean property`() {
        val instance = SimpleDataClass(enabled = true)
        assertEquals(true, instance.getPropertyValue("enabled"))
    }

    @Test
    fun `getPropertyValue gets nested object property`() {
        val nested = SimpleDataClass(name = "nested")
        val instance = NestedDataClass(simple = nested)
        assertEquals(nested, instance.getPropertyValue("simple"))
    }

    @Test
    fun `getPropertyValue gets null property`() {
        val instance = NestedDataClass(nullableValue = null)
        assertNull(instance.getPropertyValue("nullableValue"))
    }

    @Test
    fun `getPropertyValue gets non-null nullable property`() {
        val instance = NestedDataClass(nullableValue = "value")
        assertEquals("value", instance.getPropertyValue("nullableValue"))
    }

    // ==================== callCopy Tests ====================

    @Test
    fun `callCopy copies with single property change`() {
        val original = SimpleDataClass(name = "original", count = 5)
        val copied = original.callCopy("name" to "modified")

        assertEquals("modified", copied.name)
        assertEquals(5, copied.count) // Unchanged
        assertEquals(false, copied.enabled) // Unchanged (default)
    }

    @Test
    fun `callCopy copies with multiple property changes`() {
        val original = SimpleDataClass(name = "original", count = 5, enabled = false)
        val copied = original.callCopy("name" to "modified", "enabled" to true)

        assertEquals("modified", copied.name)
        assertEquals(5, copied.count) // Unchanged
        assertEquals(true, copied.enabled)
    }

    @Test
    fun `callCopy copies with all properties changed`() {
        val original = SimpleDataClass(name = "a", count = 1, enabled = false)
        val copied = original.callCopy(
            "name" to "b",
            "count" to 2,
            "enabled" to true
        )

        assertEquals("b", copied.name)
        assertEquals(2, copied.count)
        assertEquals(true, copied.enabled)
    }

    @Test
    fun `callCopy copies nested data class`() {
        val original = NestedDataClass(
            id = 1,
            simple = SimpleDataClass(name = "original"),
            nullableValue = "value"
        )
        val newSimple = SimpleDataClass(name = "modified")
        val copied = original.callCopy("simple" to newSimple)

        assertEquals(1, copied.id) // Unchanged
        assertEquals("modified", copied.simple.name)
        assertEquals("value", copied.nullableValue) // Unchanged
    }

    @Test
    fun `callCopy sets nullable to null`() {
        val original = NestedDataClass(nullableValue = "not null")
        val copied = original.callCopy("nullableValue" to null)

        assertNull(copied.nullableValue)
    }

    @Test
    fun `callCopy sets nullable to value`() {
        val original = NestedDataClass(nullableValue = null)
        val copied = original.callCopy("nullableValue" to "now has value")

        assertEquals("now has value", copied.nullableValue)
    }

    @Test
    fun `callCopy preserves object identity for unchanged properties`() {
        val nested = SimpleDataClass(name = "preserved")
        val original = NestedDataClass(id = 1, simple = nested)
        val copied = original.callCopy("id" to 2)

        // The nested object should be the same instance since it wasn't changed
        assertTrue(copied.simple === nested)
    }
}
