package com.airbnb.mvrx.launcher

import androidx.annotation.WorkerThread
import com.airbnb.mvrx.mocking.MockableMavericksView
import com.airbnb.mvrx.mocking.MockedViewProvider
import com.airbnb.mvrx.mocking.getMockVariants
import dalvik.system.BaseDexClassLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Modifier

/**
 * Provides all of the mocks declared on MavericksViews in the app.
 */
object MavericksGlobalMockLibrary {

    /**
     * Returns all of the mocks declared on MavericksViews in the app.
     *
     * This should be accessed in a background thread since this data may be loaded synchronously when accessed!
     *
     * TODO Access mocks from annotation generated code before falling back to dex approach.
     */
    @WorkerThread
    fun getMocks() = viewsFromDex

    /**
     * Uses dex analysis to detect all MavericksViews in the app.
     *
     * This uses a lot of reflection and will take a few seconds to initialize the first time it is accessed.
     * Intended for testing only, and should only be accessed in a background thread!
     */
    @get:WorkerThread
    private val viewsFromDex: List<MockedViewProvider<*>> by lazy {
        runBlocking {
            val classLoader = MavericksGlobalMockLibrary::class.java.classLoader as BaseDexClassLoader
            loadMocks(classLoader)
        }
    }
}

private suspend fun loadMocks(classLoader: BaseDexClassLoader): List<MockedViewProvider<*>> {
    return getDexFiles(classLoader)
        .asSequence()
        .flatMap { dexFile ->
            @Suppress("DEPRECATION")
            dexFile.entries().asSequence()
        }
        .filterNot { dexFileEntry ->
            // These are optimizations to avoid having to check every class in the dex files.
            dexFileEntry.startsWith("java.") ||
                dexFileEntry.startsWith("android.") ||
                dexFileEntry.startsWith("androidx.")
            // TODO: Allow mavericks configuration to specify package name prefix whitelist, or
            //  more generally, naming whitelist to identify views, for faster initialization
        }
        .map { GlobalScope.async { getMocksForClassName(it, classLoader) } }
        .toList()
        .awaitAll()
        .filterNotNull()
        .flatten()
}

private fun getMocksForClassName(
    className: String,
    classLoader: ClassLoader
): List<MockedViewProvider<*>>? {

    val clazz = try {
        classLoader.loadClass(className)
    } catch (e: ClassNotFoundException) {
        return null
    }

    return if (!Modifier.isAbstract(clazz.modifiers) && MockableMavericksView::class.java.isAssignableFrom(clazz)) {
        @Suppress("UNCHECKED_CAST")
        (getMockVariants(clazz as Class<MockableMavericksView>))
    } else {
        null
    }
}
