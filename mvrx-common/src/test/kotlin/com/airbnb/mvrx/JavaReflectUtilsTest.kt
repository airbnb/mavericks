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

    // ==================== Field Ordering Debug Tests ====================

    // Data class that mimics ClaimState with many fields of mixed types
    // This tests that field ordering matches constructor parameter ordering
    data class ManyFieldsDataClass(
        val existingItems: List<String> = emptyList(),
        val confirmationCode: String? = null,
        val referenceId: String? = null,
        val createType: String? = null,
        val currentUserId: Long = 0L,
        val messageText: String? = null,
        val agreedToTerms: Boolean = false,
        val confirmationType: String? = null,
        val reloadNeeded: Boolean = false,
        val itemToDelete: String? = null,
        // mutation fields
        val mutatedOverview: String? = null,
        val mutatedDate: String? = null,
        val mutatedTypes: Set<String> = emptySet(),
        val mutatedScenarios: Set<String> = emptySet(),
        // responses - these are the fields we want to set
        val itemResponse: Result<String>? = null,
        val statusResponse: Result<String>? = null,
        val historyResponse: Result<String>? = null,
        val saveResponse: Result<String>? = null,
        val triageResponse: Result<String>? = null,
        val eligibilityResponse: Result<String>? = null,
        val submitResponse: Result<String>? = null,
        val escalateResponse: Result<String>? = null,
        val deleteResponse: Result<String>? = null,
        val retractResponse: Result<String>? = null,
        val closeResponse: Result<String>? = null,
    )

    @Test
    fun `callCopy works with many fields data class - set middle field`() {
        val original = ManyFieldsDataClass(
            confirmationCode = "ABC123",
            referenceId = "REF-001",
            mutatedOverview = "original overview"
        )

        // Try to set a field in the middle of the class
        val copied = original.callCopy("itemResponse" to Result.success("new response"))

        assertEquals(Result.success("new response"), copied.itemResponse)
        assertEquals("ABC123", copied.confirmationCode) // Should be unchanged
        assertEquals("REF-001", copied.referenceId) // Should be unchanged
        assertEquals("original overview", copied.mutatedOverview) // Should be unchanged
    }

    @Test
    fun `callCopy works with many fields data class - set last field`() {
        val original = ManyFieldsDataClass(
            confirmationCode = "ABC123",
        )

        // Try to set the last field
        val copied = original.callCopy("closeResponse" to Result.success("closed"))

        assertEquals(Result.success("closed"), copied.closeResponse)
        assertEquals("ABC123", copied.confirmationCode) // Should be unchanged
    }

    @Test
    fun `callCopy setting one field does not modify other fields of same type`() {
        // This test verifies that setting one String field doesn't accidentally
        // modify other String fields, which could happen if field indices are wrong
        val original = ManyFieldsDataClass(
            confirmationCode = "code1",
            referenceId = "ref1",
            createType = "type1",
            messageText = "msg1",
            confirmationType = "confType1",
            itemToDelete = "item1",
            mutatedOverview = "overview1",
            mutatedDate = "date1",
        )

        // Set just one field
        val copied = original.callCopy("referenceId" to "MODIFIED")

        // Verify only the target field changed
        assertEquals("MODIFIED", copied.referenceId)

        // Verify all other String fields are unchanged
        assertEquals("code1", copied.confirmationCode)
        assertEquals("type1", copied.createType)
        assertEquals("msg1", copied.messageText)
        assertEquals("confType1", copied.confirmationType)
        assertEquals("item1", copied.itemToDelete)
        assertEquals("overview1", copied.mutatedOverview)
        assertEquals("date1", copied.mutatedDate)
    }

    @Test
    fun `callCopy setting Response field with same-type String fields`() {
        // This mimics the ClaimState issue: setting an Async/Result response field
        // when there are multiple String fields earlier in the class
        val original = ManyFieldsDataClass(
            confirmationCode = "ABC",
            referenceId = "REF",
            createType = "TYPE",
        )

        val copied = original.callCopy("statusResponse" to Result.success("new status"))

        // The Response field should be set
        assertEquals(Result.success("new status"), copied.statusResponse)

        // All String fields should be unchanged
        assertEquals("ABC", copied.confirmationCode)
        assertEquals("REF", copied.referenceId)
        assertEquals("TYPE", copied.createType)
    }

    // ==================== Field Reordering Simulation Tests ====================
    // These tests simulate R8/ProGuard field reordering by using a data class
    // and explicitly verifying the componentN-based index lookup.

    @Test
    fun `buildPropertyIndexMap correctly maps field names to indices`() {
        val instance = ManyFieldsDataClass(
            confirmationCode = "test-code",
            referenceId = "test-ref",
            currentUserId = 42L,
            itemResponse = Result.success("test-item"),
            closeResponse = Result.success("test-close"),
        )

        val indexMap = instance.buildPropertyIndexMapForTesting()

        // Verify that known properties map to their correct indices
        // (indices match constructor parameter order)
        assertEquals(0, indexMap["existingItems"])
        assertEquals(1, indexMap["confirmationCode"])
        assertEquals(2, indexMap["referenceId"])
        assertEquals(3, indexMap["createType"])
        assertEquals(4, indexMap["currentUserId"])
        assertEquals(14, indexMap["itemResponse"])
        assertEquals(24, indexMap["closeResponse"])
    }

    // ==================== Body Property Tests ====================
    // Tests that body properties (non-constructor properties with backing fields)
    // don't interfere with constructor property mapping.

    /**
     * Data class with body properties that have the same type and default value
     * as constructor parameters. This mimics the PayoutMethodManagementState issue
     * where body properties like 'addPayoutMethodButtonLoadingState' could incorrectly
     * "steal" the component slot from constructor property 'showTaxPayerInformationModal'.
     */
    data class DataClassWithBodyProperties(
        val firstBool: Boolean = false,
        val secondBool: Boolean = false,
        val thirdBool: Boolean = false,
        val name: String = "default",
    ) {
        // Body properties - these have backing fields but no componentN methods
        val derivedBool: Boolean = firstBool || secondBool
        val anotherDerivedBool: Boolean = name.isEmpty()
    }

    @Test
    fun `callCopy works with data class that has body properties`() {
        val original = DataClassWithBodyProperties()

        // All booleans start as false, including body properties
        // Setting thirdBool should NOT affect other boolean fields
        val copied = original.callCopy("thirdBool" to true)

        assertEquals(false, copied.firstBool)
        assertEquals(false, copied.secondBool)
        assertEquals(true, copied.thirdBool)
        assertEquals("default", copied.name)
    }

    @Test
    fun `callCopy sets multiple boolean constructor params with body properties present`() {
        val original = DataClassWithBodyProperties()

        val copied = original.callCopy(
            "firstBool" to true,
            "thirdBool" to true,
        )

        assertEquals(true, copied.firstBool)
        assertEquals(false, copied.secondBool)
        assertEquals(true, copied.thirdBool)
    }

    @Test
    fun `buildPropertyIndexMap excludes body properties`() {
        val instance = DataClassWithBodyProperties()
        val indexMap = instance.buildPropertyIndexMapForTesting()

        // Should have 4 constructor parameters mapped
        assertEquals(4, indexMap.size)

        // Constructor parameters should be mapped
        assertEquals(0, indexMap["firstBool"])
        assertEquals(1, indexMap["secondBool"])
        assertEquals(2, indexMap["thirdBool"])
        assertEquals(3, indexMap["name"])

        // Body properties should NOT be in the map
        assertNull(indexMap["derivedBool"])
        assertNull(indexMap["anotherDerivedBool"])
    }

    /**
     * Test helper to expose the internal index map building.
     */
    private fun <T : Any> T.buildPropertyIndexMapForTesting(): Map<String, Int> {
        return buildPropertyIndexMap(this::class.java, this)
    }

    /**
     * Builds a mapping from property names to their constructor parameter index.
     * Uses componentN methods which preserve constructor order even when fields are reordered.
     * Iterates componentN methods first to ensure body properties are excluded.
     */
    private fun <T : Any> buildPropertyIndexMap(jvmClass: Class<*>, instance: T): Map<String, Int> {
        // componentN methods are named component1, component2, etc.
        // N corresponds to constructor parameter position (1-indexed)
        // These may have suffixes like "component1-abc" for value classes or "component1$module" for internal
        val componentRegex = Regex("""component(\d+).*""")
        val componentMethods = jvmClass.declaredMethods
            .mapNotNull { method ->
                if (method.parameterCount != 0) return@mapNotNull null
                val match = componentRegex.matchEntire(method.name) ?: return@mapNotNull null
                val n = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
                n to method
            }
            .sortedBy { it.first }

        // Get all instance fields
        val fields = jvmClass.declaredFields
            .filter { !java.lang.reflect.Modifier.isStatic(it.modifiers) && !it.isSynthetic }

        val result = mutableMapOf<String, Int>()
        val usedFields = mutableSetOf<String>()

        // For each componentN method, find the field that matches its value.
        // This ensures we only map constructor parameters (which have componentN methods)
        // and not body properties (which don't have componentN methods).
        for ((n, componentMethod) in componentMethods) {
            val index = n - 1 // componentN is 1-indexed, parameters are 0-indexed
            componentMethod.isAccessible = true
            val componentValue = componentMethod.invoke(instance)

            for (field in fields) {
                if (field.name in usedFields) continue

                // Type must match to avoid ambiguity with same-value fields of different types
                if (field.type != componentMethod.returnType) continue

                field.isAccessible = true
                val fieldValue = field.get(instance)

                if (fieldValue == componentValue) {
                    result[field.name] = index
                    usedFields.add(field.name)
                    break
                }
            }
        }

        return result
    }
}
