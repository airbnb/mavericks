package com.airbnb.mvrx.mocking.printer

/**
 * Set configuration details for how mock state is generated.
 */
class MockPrinterConfiguration(
    /**
     * Given a [MavericksView] that we are generating mock states for, returns which package name to use
     * for the generated file.
     *
     * By default mock states are place in a "mocks" subpackage within the package of the view
     * it is mocking.
     */
    val mockPackage: (mockedView: Any) -> String = { mockedView ->
        val packageName = mockedView::class.qualifiedName!!.substringBeforeLast(".")
        "$packageName.mocks"
    },
    /**
     * Define functions for generating code for a custom class.
     *
     * This allows the mock printer to generate code to construct instances of a custom type.
     * By default, primitive types, enums, collections, Kotlin 'object's, AutoValue classes,
     * and Kotlin data classes are supported.
     *
     * These are called in order. The first one to return true from [TypePrinter.acceptsObject] will
     * be used, otherwise a default implementation will be used for the object.
     */
    val customTypePrinters: List<TypePrinter<*>> = emptyList()
)
