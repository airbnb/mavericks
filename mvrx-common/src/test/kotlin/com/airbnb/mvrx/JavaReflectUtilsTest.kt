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
    fun `callCopy maps fields correctly across many properties`() {
        val instance = ManyFieldsDataClass(
            confirmationCode = "test-code",
            referenceId = "test-ref",
            currentUserId = 42L,
            itemResponse = Result.success("test-item"),
            closeResponse = Result.success("test-close"),
        )

        // Verify setting fields at various positions works correctly
        val copied = instance.callCopy(
            "existingItems" to listOf("new"),
            "createType" to "modified",
            "closeResponse" to Result.success("modified-close"),
        )

        assertEquals(listOf("new"), copied.existingItems)
        assertEquals("test-code", copied.confirmationCode) // unchanged
        assertEquals("test-ref", copied.referenceId) // unchanged
        assertEquals("modified", copied.createType)
        assertEquals(42L, copied.currentUserId) // unchanged
        assertEquals(Result.success("test-item"), copied.itemResponse) // unchanged
        assertEquals(Result.success("modified-close"), copied.closeResponse)
    }

    // ==================== Body Property Tests ====================
    // Tests that body properties (non-constructor properties with backing fields)
    // don't interfere with constructor property mapping.

    data class DataClassWithBodyProperties(
        val firstBool: Boolean = false,
        val secondBool: Boolean = false,
        val thirdBool: Boolean = false,
        val name: String = "default",
    ) {
        val derivedBool: Boolean = firstBool || secondBool
        val anotherDerivedBool: Boolean = name.isEmpty()
    }

    @Test
    fun `callCopy works with data class that has body properties`() {
        val original = DataClassWithBodyProperties()
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

    // ==================== Sequential callCopy (Mock Builder Pattern) ====================
    // The Mavericks mock builder calls callCopy repeatedly on successive state instances.
    // After one callCopy sets a boolean to true, a body property that is also true can
    // collide with the next callCopy's field matching if using pure value-based matching.

    @Test
    fun `callCopy works after prior copy changes boolean - body property value collision`() {
        // Simulates the PayoutMethodManagementState CI failure:
        // 1. Initial state: all constructor bools false, body prop derivedBool = false
        // 2. First callCopy sets secondBool=true → derivedBool becomes true
        // 3. Second callCopy on the NEW instance tries to set thirdBool=true
        //    At this point, secondBool=true AND derivedBool=true (both Boolean, same value)
        //    Without proper disambiguation, derivedBool could steal secondBool's component slot
        val step1 = DataClassWithBodyProperties()
        val step2 = step1.callCopy("secondBool" to true)

        assertEquals(true, step2.secondBool)
        assertEquals(true, step2.derivedBool) // body prop is now also true

        // This callCopy should still correctly identify thirdBool
        val step3 = step2.callCopy("thirdBool" to true)

        assertEquals(false, step3.firstBool)
        assertEquals(true, step3.secondBool)
        assertEquals(true, step3.thirdBool)
    }

    /**
     * Mimics PayoutMethodManagementState structure more closely: multiple Async-like
     * fields with the same default value, plus boolean fields with body properties.
     */
    data class StateWithAsyncAndBoolBodyProps(
        val response1: String? = null,
        val response2: String? = null,
        val dismissedAlert: Boolean = false,
        val isRequired: Boolean = false,
        val showModal: Boolean = false,
        val info: String? = null,
        val response3: String? = null,
        val items: Map<String, String?> = emptyMap(),
    ) {
        val isLoading: Boolean = response1 == null
        val buttonDisabled: Boolean = response1 == null || response3 != null
    }

    @Test
    fun `callCopy on state with multiple same-type fields and body properties`() {
        val original = StateWithAsyncAndBoolBodyProps()
        // isLoading = true (body prop), buttonDisabled = true (body prop)

        // Simulate mock builder setting isRequired=true first
        val step1 = original.callCopy("isRequired" to true)
        assertEquals(true, step1.isRequired)
        assertEquals(false, step1.showModal)

        // Then setting showModal=true (the field that was failing in CI)
        val step2 = step1.callCopy("showModal" to true)
        assertEquals(true, step2.isRequired)
        assertEquals(true, step2.showModal)
        assertEquals(false, step2.dismissedAlert)
    }

    @Test
    fun `callCopy correctly handles multiple null reference fields`() {
        val original = StateWithAsyncAndBoolBodyProps()
        val copied = original.callCopy("response2" to "updated")

        assertNull(copied.response1)
        assertEquals("updated", copied.response2)
        assertNull(copied.info)
        assertNull(copied.response3)
    }

    // ==================== toString Parsing Tests ====================

    @Test
    fun `callCopy works when toString has nested data class values`() {
        val original = NestedDataClass(
            id = 1,
            simple = SimpleDataClass(name = "inner"),
            nullableValue = "test",
        )
        val copied = original.callCopy("nullableValue" to "changed")

        assertEquals(1L, copied.id)
        assertEquals("inner", copied.simple.name)
        assertEquals("changed", copied.nullableValue)
    }

    @Test
    fun `callCopy works with data class where all fields have same type and value`() {
        data class AllSameDefaults(
            val a: Boolean = false,
            val b: Boolean = false,
            val c: Boolean = false,
            val d: Boolean = false,
        )

        val original = AllSameDefaults()
        val copied = original.callCopy("c" to true)

        assertEquals(false, copied.a)
        assertEquals(false, copied.b)
        assertEquals(true, copied.c)
        assertEquals(false, copied.d)
    }
}
