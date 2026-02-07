package com.airbnb.mvrx.mocking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for DataClassSetDsl integration with the Java reflection utilities.
 * The core utility tests (isData, callCopy, getPropertyValue) are in mvrx-common.
 */
class DataClassSetDslIntegrationTest {

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

    // ==================== Integration Tests with DataClassSetDsl ====================

    @Test
    fun `DataClassSetDsl set works with Java reflection`() {
        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = SimpleDataClass(name = "original", count = 10)
            val modified = original.set { ::name }.with { "modified" }

            assertEquals("modified", modified.name)
            assertEquals(10, modified.count)
        }
    }

    @Test
    fun `DataClassSetDsl set works with nested properties`() {
        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = NestedDataClass(
                id = 1,
                simple = SimpleDataClass(name = "nested", count = 5)
            )
            val modified = original.set { ::simple { ::name } }.with { "modified nested" }

            assertEquals(1, modified.id)
            assertEquals("modified nested", modified.simple.name)
            assertEquals(5, modified.simple.count)
        }
    }

    @Test
    fun `DataClassSetDsl set works with deeply nested properties`() {
        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = DeeplyNestedDataClass(
                level1 = NestedDataClass(
                    id = 1,
                    simple = SimpleDataClass(name = "deep")
                )
            )
            val modified = original.set { ::level1 { ::simple { ::name } } }.with { "very deep" }

            assertEquals("very deep", modified.level1.simple.name)
            assertEquals(1, modified.level1.id)
        }
    }

    @Test
    fun `DataClassSetDsl setNull works`() {
        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = NestedDataClass(nullableValue = "not null")
            val modified = original.setNull { ::nullableValue }

            assertNull(modified.nullableValue)
        }
    }

    @Test
    fun `DataClassSetDsl setTrue works`() {
        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = SimpleDataClass(enabled = false)
            val modified = original.setTrue { ::enabled }

            assertTrue(modified.enabled!!)
        }
    }

    @Test
    fun `DataClassSetDsl setFalse works`() {
        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = SimpleDataClass(enabled = true)
            val modified = original.setFalse { ::enabled }

            assertFalse(modified.enabled!!)
        }
    }

    @Test
    fun `DataClassSetDsl setZero works`() {
        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = SimpleDataClass(count = 100)
            val modified = original.setZero { ::count }

            assertEquals(0, modified.count)
        }
    }

    @Test
    fun `DataClassSetDsl setEmpty works`() {
        data class WithList(val items: List<String> = listOf("a", "b"))

        val dsl = object : DataClassSetDsl {}

        with(dsl) {
            val original = WithList()
            val modified = original.setEmpty { ::items }

            assertTrue(modified.items.isEmpty())
        }
    }
}
