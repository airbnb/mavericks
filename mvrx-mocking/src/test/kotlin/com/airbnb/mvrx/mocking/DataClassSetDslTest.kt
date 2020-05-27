package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Uninitialized
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataClassSetDslTest : BaseTest(), DataClassSetDsl {

    @Test
    fun setSingleLayer() {
        TestClass().set { ::num }.with { 1 }.apply {
            assertEquals(1, num)
        }
    }

    @Test
    fun setSecondLayer() {
        TestClass().set { ::test2 { ::nullableNum } }.with { 1 }.apply {
            assertEquals(1, test2.nullableNum)
        }
    }

    @Test
    fun setThirdLayer() {
        TestClass().set { ::test2 { ::test3 { ::str } } }.with { "hi" }.apply {
            assertEquals("hi", test2.test3.str)
        }
    }

    @Test
    fun setZero() {
        TestClass(num = 5).setZero { ::num }.apply {
            assertEquals(0, num)
        }
    }

    @Test
    fun setTrue() {
        TestClass(bool = false).setTrue { ::bool }.apply {
            assertEquals(true, bool)
        }
    }

    @Test
    fun setFalse() {
        TestClass(bool = true).setFalse { ::bool }.apply {
            assertEquals(false, bool)
        }
    }

    @Test
    fun setEmpty() {
        TestClass(list = listOf(1)).setEmpty { ::list }.apply {
            assertEquals(emptyList<Int>(), list)
        }
    }

    @Test
    fun setNull() {
        TestClass(test2 = TestClass2(nullableNum = 1)).setNull { ::test2 { ::nullableNum } }.apply {
            assertNull(test2.nullableNum)
        }
    }

    @Test
    fun setAsyncLoading() {
        TestClass().setLoading { ::async }.apply {
            assertEquals(Loading<Int>(), async)
        }
    }

    @Test
    fun setAsyncFail() {
        TestClass().setNetworkFailure { ::async }.apply {
            assertTrue(async is Fail)
        }
    }
}

data class TestClass(
    val num: Int = 0,
    val bool: Boolean = false,
    val list: List<Int> = emptyList(),
    val async: Async<Int> = Uninitialized,
    val test2: TestClass2 = TestClass2()
)

data class TestClass2(
    val nullableNum: Int? = 0,
    val test3: TestClass3 = TestClass3()

)

data class TestClass3(
    val str: String = ""
)