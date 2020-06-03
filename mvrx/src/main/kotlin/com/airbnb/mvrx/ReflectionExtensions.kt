package com.airbnb.mvrx

internal val primitiveWrapperMap = mapOf(
    Boolean::class.javaPrimitiveType to Boolean::class.java,
    Byte::class.javaPrimitiveType to Byte::class.javaObjectType,
    Char::class.javaPrimitiveType to Char::class.javaObjectType,
    Double::class.javaPrimitiveType to Double::class.javaObjectType,
    Float::class.javaPrimitiveType to Float::class.javaObjectType,
    Int::class.javaPrimitiveType to Int::class.javaObjectType,
    Long::class.javaPrimitiveType to Long::class.javaObjectType,
    Short::class.javaPrimitiveType to Short::class.javaObjectType
)

internal fun isPrimitiveWrapperOf(targetClass: Class<*>, primitive: Class<*>): Boolean {
    require(primitive.isPrimitive) { "First argument has to be primitive type" }
    return primitiveWrapperMap[primitive] == targetClass
}

internal fun isAssignableTo(from: Class<*>, to: Class<*>): Boolean {
    if (to.isAssignableFrom(from)) {
        return true
    }
    if (from.isPrimitive) {
        return isPrimitiveWrapperOf(to, from)
    }
    return if (to.isPrimitive) {
        isPrimitiveWrapperOf(from, to)
    } else false
}
