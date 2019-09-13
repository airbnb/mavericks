package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseTest
import com.airbnb.mvrx.MvRxState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstructorCodeTest : BaseTest() {

    @Test
    fun testImports() {
        val code = ConstructorCode(State(), Integer.MAX_VALUE, Integer.MAX_VALUE)

        assertEquals(
            "com.airbnb.mvrx.mock.ConstructorCodeTest.State, kotlin.Int, kotlin.Float, kotlin.String, kotlin.Double, com.airbnb.mvrx.mock.ConstructorCodeTest.NestedObject, com.airbnb.mvrx.mock.ConstructorCodeTest.MyEnum, com.airbnb.mvrx.mock.ConstructorCodeTest.MySingleton",
            code.imports.joinToString()
        )
    }

    @Test
    fun testConstructor() {
        val code = ConstructorCode(State(), Integer.MAX_VALUE, Integer.MAX_VALUE)

        assertEquals(
            "val mockState by lazy { State(\n" +
                    "int = 1,\n" +
                    "float = 1.0f,\n" +
                    "str = \"hello\",\n" +
                    "charSequence = \"'hi' with nested \\\"quotes\\\" and \\ta tab\",\n" +
                    "double = 4.5,\n" +
                    "map = mutableMapOf(3 to \"three\",2 to \"two\"),\n" +
                    "strList = listOf(\n" +
                    "\"hi\",\n" +
                    "\"there\"\n" +
                    "),\n" +
                    "nestedObject = NestedObject(myEnum = MyEnum.A),\n" +
                    "singleTon = MySingleton,\n" +
                    "nestedObjectList = listOf(\n" +
                    "NestedObject(myEnum = MyEnum.A)\n" +
                    ")\n" +
                    ") }",
            code.code
        )
    }

    @Test
    fun testJsonObject() {
        val code = ConstructorCode(StateWithJsonObject(), Integer.MAX_VALUE, Integer.MAX_VALUE)

        assertEquals(
            "val mockStateWithJsonObject by lazy { StateWithJsonObject(json = \"\"\"{\n" +
                    "  \"color\": \"red\",\n" +
                    "  \"numbers\": [\n" +
                    "    {\n" +
                    "      \"favorite\": 7\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"lowest\": 0\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\"\"\") }",
            code.code
        )
    }

    @Test
    fun testJsonObjectNotTruncated() {
        val code = ConstructorCode(StateWithJsonObject(), Integer.MAX_VALUE, 3)

        assertEquals(
            "val mockStateWithJsonObject by lazy { StateWithJsonObject(json = \"\"\"{\n" +
                    "  \"color\": \"red\",\n" +
                    "  \"numbers\": [\n" +
                    "    {\n" +
                    "      \"favorite\": 7\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"lowest\": 0\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\"\"\") }",
            code.code
        )
    }

    @Test
    fun testJsonArray() {
        val code = ConstructorCode(StateWithJsonArray(), Integer.MAX_VALUE, Integer.MAX_VALUE)

        assertEquals(
            "val mockStateWithJsonArray by lazy { StateWithJsonArray(json = \"\"\"[\n" +
                    "  {\n" +
                    "    \"favorite\": 7\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"lowest\": 0\n" +
                    "  }\n" +
                    "]\"\"\") }",
            code.code
        )
    }

    @Test
    fun testInvalidJson() {
        val code =
            ConstructorCode(StateWithInvalidJsonObject(), Integer.MAX_VALUE, Integer.MAX_VALUE)

        assertEquals(
            "val mockStateWithInvalidJsonObject by lazy { StateWithInvalidJsonObject(json = \"not valid{\\\"color\\\":\\\"red\\\",\\\"numbers\\\":[{\\\"favorite\\\":7},{\\\"lowest\\\":0}]}\") }",
            code.code
        )
    }

    @Test
    fun testLazy() {
        val code = ConstructorCode(StateWithLazy(), Integer.MAX_VALUE, Integer.MAX_VALUE)

        assertEquals(
            "val mockStateWithLazy by lazy { StateWithLazy(lazyInt = lazy { 1 }) }",
            code.code
        )
    }


    @Test
    fun testCustomTypePrinter() {
        data class Test(
            val date: CustomDate = CustomDate.fromString("2000")
        ) : MvRxState

        val result = ConstructorCode(
            Test(),
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            customTypePrinters = listOf(
                typePrinter<CustomDate>(
                    transformImports = { it.plus("hello world") },
                    codeGenerator = { date, _ -> "CustomDate.fromString(\"${date.asString()}\")" }
                )
            )
        )

        result.expect("Test(date = CustomDate.fromString(\"2000\"))")

        assertTrue(result.imports.contains("hello world"))
    }

    @Test
    fun listIsTruncated() {
        data class Test(
            val list: List<Int> = listOf(1, 2, 3, 4)
        ) : MvRxState

        ConstructorCode(Test(), 3, 200).expect("Test(list = listOf(\n1,\n2,\n3\n))")
    }

    @Test
    fun listIsNotTruncated() {
        data class Test(
            val list: List<Int> = listOf(1, 2, 3, 4)
        ) : MvRxState

        ConstructorCode(
            Test(),
            Integer.MAX_VALUE,
            Integer.MAX_VALUE
        ).expect("Test(list = listOf(\n1,\n2,\n3,\n4\n))")
    }

    @Test
    fun listIsNotTruncatedWhenTypesDiffer() {
        data class Test(
            val list: List<Any> = listOf(1, 2, 3, "A")
        ) : MvRxState

        ConstructorCode(Test(), 3, 200).expect("Test(list = listOf(\n1,\n2,\n3,\n\"A\"\n))")
    }

    @Suppress("ArrayInDataClass")
    @Test
    fun arrayIsTruncated() {
        data class Test(
            val list: Array<Int> = arrayOf(1, 2, 3, 4)
        ) : MvRxState

        ConstructorCode(Test(), 3, 200).expect("Test(list = arrayOf(\n1,\n2,\n3\n))")
    }

    @Suppress("ArrayInDataClass")
    @Test
    fun arrayIsNotTruncated() {
        data class Test(
            val list: Array<Int> = arrayOf(1, 2, 3, 4)
        ) : MvRxState

        ConstructorCode(
            Test(),
            Integer.MAX_VALUE,
            Integer.MAX_VALUE
        ).expect("Test(list = arrayOf(\n1,\n2,\n3,\n4\n))")
    }

    private fun <T : MvRxState> ConstructorCode<T>.expect(expectedCode: String) {
        assertEquals("val mockTest by lazy { $expectedCode }", code)
    }

    data class StateWithJsonObject(val json: String = """{"color":"red","numbers":[{"favorite":7},{"lowest":0}]}""") :
        MvRxState

    data class StateWithInvalidJsonObject(val json: String = """not valid{"color":"red","numbers":[{"favorite":7},{"lowest":0}]}""") :
        MvRxState

    data class StateWithJsonArray(val json: String = """[{"favorite":7},{"lowest":0}]""") :
        MvRxState

    data class NestedObject(val nullableInt: Int? = null, val myEnum: MyEnum = MyEnum.A)

    data class StateWithLazy(val lazyInt: Lazy<Int> = lazy { 1 })

    enum class MyEnum {
        A
    }

    object MySingleton

    data class State(
        val int: Int = 1,
        val float: Float = 1f,
        val boolean: Boolean = false,
        val str: String = "hello",
        val charSequence: CharSequence = "'hi' with nested \"quotes\" and \ta tab",
        val double: Double = 4.5,
        val map: Map<Int, String> = mapOf(3 to "three", 2 to "two"),
        val strList: List<String> = listOf("hi", "there"),
        val nestedObject: NestedObject = NestedObject(),
        val singleTon: MySingleton = MySingleton,
        val nestedObjectList: List<NestedObject> = listOf(NestedObject())
    ) : MvRxState

    class CustomDate private constructor(private val time: Long) {
        fun asString(): String = time.toString()

        companion object {
            fun fromString(dateString: String) = CustomDate(dateString.toLong())
        }
    }
}
