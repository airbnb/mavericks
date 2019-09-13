package com.airbnb.mvrx.mock

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/**
 * This utility is used to print the code need to construct an object. It is useful for printing out a MvRxState instance's
 * constructor code when setting up a test for that state.
 *
 * It uses recursion to analyze the given object instance and
 * generates code needed to construct another instance of the object containing the same data.
 */
internal class ConstructorCode<T : Any>(
    objectToCopy: T,
    private val listTruncationThreshold: Int,
    private val stringTruncationThreshold: Int,
    private val customTypePrinters: List<TypePrinter<*>> = emptyList()
) {
    private val usedTypePrinters = mutableListOf<TypePrinter<*>>()
    val dependencies = mutableSetOf<KClass<*>>()
    val code =
        "val mock${objectToCopy::class.simpleName} by lazy { ${objectToCopy.getConstructor()} }"

    val imports: List<String> = run {
        val defaultImports = dependencies.mapNotNull { it.qualifiedName }
        usedTypePrinters.fold(defaultImports) { imports, typePrinter ->
            typePrinter.modifyImports(imports)
        }
    }

    private fun Any?.getConstructor(): String {

        return when (this) {
            null -> "null"
            is List<*> -> "listOf(${listItemsCode()})"
            is Array<*> -> "arrayOf(${arrayItemsCode()})"
            is Map<*, *> -> getMapConstructor()
            else -> null
        } ?: run {

            val kClass = this!!::class
            dependencies.add(kClass)

            val simpleName = kClass.simpleName.orEmpty()

            val customResult = customTypePrinters
                .firstOrNull { it.acceptsObject(this) }
                ?.let {
                    usedTypePrinters.add(it)
                    @Suppress("UNCHECKED_CAST")
                    it as TypePrinter<Any>
                }
                ?.generateCode(this) { it.getConstructor() }

            when {
                customResult != null -> customResult
                this is CharSequence -> getCharSequenceConstructor()
                this is Float -> toString() + "f"
                this is Lazy<*> -> "lazy { ${this.value.getConstructor()} }"
                kClass.isObjectInstance -> simpleName
                kClass.isEnum -> simpleName + "." + toString()
                kClass.isKotlinClass -> getKotlinConstructor()
                //  Don't know how to handle this type, falling back to toString()
                else -> toString()
            }
        }
    }

    private fun String.escape() = "\"$this\""

    private fun String.escapeWithTripleQuotes() = "\"\"\"$this\"\"\""

    private fun CharSequence.getCharSequenceConstructor(): String {
        // Escaping a JSON string with single quotes leads to an unreadable mess due to being on a single line and
        // escaping the inner quotes. Let's format (newlines and indentations) and remove the need to escape the quotes.
        toHumanReadableJson(this.toString())
            ?.escapeWithTripleQuotes()
            ?.also { return it }

        return toString()
            .replace(
                "\"",
                "\\\""
            ) // Make sure quotes inside the string are escaped, since we also wrap with ""
            .replace("\n", "\\n") // Preserve new lines in the mock data
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .take(stringTruncationThreshold) // Truncation prevents unnecessarily large mock data
            .escape()
    }

    /**
     * If this string is a representation of a [JSONObject] or a [JSONArray] then return the
     * human-readable representation. Else, return null.
     */
    private fun toHumanReadableJson(str: String): String? {
        return try {
            JSONObject(str).toString(2)
        } catch (e: JSONException) {
            try {
                JSONArray(str).toString(2)
            } catch (e: JSONException) {
                null
            }
        }
    }

    // We only truncate if all items in the list are the same type, otherwise we may lose important data.
    // One example case is polymorphic data from a server response.
    // When they are all the same type then we likely lose nothing important by removing items, and it simplifies the mock
    private fun List<*>.listItemsCode(): String {
        val result =
            this.take(if (hasAllSameClassType()) listTruncationThreshold else Int.MAX_VALUE)
                .joinToString(separator = ",\n") { "" + it.getConstructor() }
        return "\n$result\n"
    }

    private fun Array<*>.arrayItemsCode(): String {
        val result =
            this.take(if (hasAllSameClassType()) listTruncationThreshold else Int.MAX_VALUE)
                .joinToString(separator = ",\n") { "" + it.getConstructor() }
        return "\n$result\n"
    }

    private fun Array<*>.hasAllSameClassType(): Boolean = toList().hasAllSameClassType()

    private fun List<*>.hasAllSameClassType(): Boolean = isEmpty() || all {
        val first = first()
        if ((first == null) != (it == null)) {
            // If some are null, consider them different types
            return@all false
        }

        if (first == null) {
            // If they're all null, consider them the same type
            return true
        }

        first::class == it!!::class
    }

    private fun Map<*, *>.getMapConstructor(): String {
        val params =
            entries.joinToString(separator = ",") { "${it.key.getConstructor()} to ${it.value.getConstructor()}" }

        return if (this is MutableMap<*, *>) "mutableMapOf($params)" else "mapOf($params)"
    }

    private fun Any.getKotlinConstructor(): String {
        val kClass = this::class
        val constructor = getIfReflectionSupported {
            kClass.primaryConstructor
        } ?: return "error getting primary constructor for ${kClass.simpleName}"
        val properties = kClass.memberProperties

        val params = constructor.parameters.mapNotNull { param ->
            val property =
                properties.firstOrNull { it.name == param.name } ?: return@mapNotNull null
            property.isAccessible = true
            val value = property.getter.call(this)
            if (filterKotlinClassParam(param, value)) return@mapNotNull null
            "${param.name} = ${value.getConstructor()}"
        }

        val codeOnOneLine = "${kClass.simpleName}(${params.joinToString(separator = ",")})"

        return if (params.size > DATA_CLASS_PROPERTIES_PER_LINE_LIMIT || codeOnOneLine.length > LINE_LIMIT) {
            "${kClass.simpleName}(\n${params.joinToString(separator = ",\n")}\n)"
        } else {
            codeOnOneLine
        }
    }

    private fun filterKotlinClassParam(param: KParameter, value: Any?): Boolean {
        // We can only skip params with default values
        if (!param.isOptional) return false

        // We make an assumption here that if a property's value is a common "default", like null, 0,
        // or empty, then that is it's parameter's
        // default and we can skip explicitly defining it. This helps to save a lot of
        // lines of constructor code in large classes that otherwise do
        // nothing.
        return value == null ||
                value.isPrimitiveDefault() ||
                (value is Map<*, *> && value.isEmpty()) ||
                (value is Collection<*> && value.isEmpty())
    }

    private fun Any.isPrimitiveDefault(): Boolean {
        return when (this) {
            false, 0, 0f, 0.0, 0L -> true
            else -> false
        }
    }
}

private const val LINE_LIMIT = 150
private const val DATA_CLASS_PROPERTIES_PER_LINE_LIMIT = 3
