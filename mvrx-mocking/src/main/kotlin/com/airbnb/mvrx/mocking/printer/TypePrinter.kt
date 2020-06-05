package com.airbnb.mvrx.mocking.printer

/**
 * This interface defines how to generate mock code for a custom object type.
 *
 * Use [typePrinter] for simple, basic implementation.
 */
interface TypePrinter<T : Any> {

    /** Return true if [obj] is a valid  object to pass to [generateCode]. */
    fun acceptsObject(obj: Any): Boolean

    /**
     * A function that takes an instance of an object and returns a String representing the Kotlin code
     * that can be used to recreate the instance with the same data.
     *
     * @param generateConstructor This function can be used to access the default code generation
     * for arbitrary object types. This can be used to generate code for any nested objects within
     * the target instance. Note that calling  this with [instance] will cause an infinite loop.
     */
    fun generateCode(instance: T, generateConstructor: (Any?) -> String): String

    /**
     * Optionally modify the import statements that will be added to the generated mock file.
     * By default, each processed type is added as an import.
     *
     * This function can be used if your generated code depends on anything beyond the instance
     * types that are processed.
     *
     * This will be called once per registered [TypePrinter], after all other code has been generated,
     * but only if this instance is actually used to generated code (ie it returned true from
     * [acceptsObject] at least once).
     */
    fun modifyImports(imports: List<String>): List<String> = imports
}

/**
 * Helper for easily creating a [TypePrinter].
 *
 * @param codeGenerator See [TypePrinter.generateCode]
 */
inline fun <reified T : Any> typePrinter(
    crossinline transformImports: (List<String>) -> List<String> = { it },
    crossinline codeGenerator: (instance: T, generateConstructor: (Any?) -> String) -> String
): TypePrinter<T> {
    return object : TypePrinter<T> {
        override fun acceptsObject(obj: Any): Boolean = obj is T

        override fun generateCode(instance: T, generateConstructor: (Any?) -> String): String {
            return codeGenerator(instance, generateConstructor)
        }

        override fun modifyImports(imports: List<String>): List<String> {
            return transformImports(imports)
        }
    }
}
