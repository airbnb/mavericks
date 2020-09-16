// Using DexFile is deprecated, but it doesn't matter too much to us since this is just for testing.
// Eventually we'll have to replace it with a new approach though.
@file:Suppress("DEPRECATION")

package com.airbnb.mvrx.launcher

import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import java.lang.reflect.Field

/**
 * Get all DexFiles loaded in the app.
 */
internal fun getDexFiles(classLoader: BaseDexClassLoader): List<DexFile> {
    // Here we do some reflection to access the dex files from the class loader. These implementation details vary by platform version,
    // so we have to be a little careful, but not a huge deal since this is just for testing. It should work on 21+.
    // The source for reference is at:
    // https://android.googlesource.com/platform/libcore/+/oreo-release/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java

    val pathListField = field("dalvik.system.BaseDexClassLoader", "pathList")
    val pathList = pathListField.get(classLoader) // Type is DexPathList

    val dexElementsField = field("dalvik.system.DexPathList", "dexElements")

    @Suppress("UNCHECKED_CAST")
    val dexElements =
        dexElementsField.get(pathList) as Array<Any> // Type is Array<DexPathList.Element>

    val dexFileField = field("dalvik.system.DexPathList\$Element", "dexFile")
    return dexElements.map {
        dexFileField.get(it) as DexFile
    }
}

private fun field(className: String, fieldName: String): Field {
    val clazz = Class.forName(className)
    val field = clazz.getDeclaredField(fieldName)
    field.isAccessible = true
    return field
}
