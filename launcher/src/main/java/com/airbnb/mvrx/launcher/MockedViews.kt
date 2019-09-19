package com.airbnb.mvrx.launcher

import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.mock.MockedViewProvider
import com.airbnb.mvrx.mock.getMockVariants
import dalvik.system.BaseDexClassLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Modifier

internal object MockedViews {
    /**
     * Load all MvRxViews in the app. This uses a lot of reflection and will take a few seconds to initialize the first time it is accessed.
     * Intended for testing only, and should only be accessed in a background thread!
     */
    val MOCKED_VIEW_PROVIDERS: List<MockedViewProvider<*>> by lazy {
        runBlocking {
            val classLoader = MockedViews::class.java.classLoader as BaseDexClassLoader
            loadMocks(classLoader)
        }
    }
}

private suspend fun loadMocks(classLoader: BaseDexClassLoader): List<MockedViewProvider<*>> {
    return getDexFiles(classLoader)
        .flatMap { it.entries().asSequence() }
        .filterNot {
            // These are optimizations to avoid having to check every class in the dex files.
            it.startsWith("java.") ||
                    it.startsWith("android.") ||
                    it.startsWith("androidx.")
            // TODO: Allow mvrx configuration to specify package name prefix whitelist, or
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
    val clazz = classLoader.loadClass(className)

    return if (!Modifier.isAbstract(clazz.modifiers) && MvRxView::class.java.isAssignableFrom(clazz)) {
        @Suppress("UNCHECKED_CAST")
        getMockVariants(clazz as Class<MvRxView>)
    } else {
        null
    }
}