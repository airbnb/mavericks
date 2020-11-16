package com.airbnb.mvrx.launcher

import androidx.annotation.WorkerThread
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.mocking.MockableMavericksView
import com.airbnb.mvrx.mocking.MockedViewProvider
import com.airbnb.mvrx.mocking.getMockVariants
import dalvik.system.BaseDexClassLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
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
    fun getMocks(): List<MockedViewProvider<*>> = viewsFromDex

    val mockableViewsFlow: StateFlow<Async<List<Class<out MockableMavericksView>>>> by lazy {
        val classLoader = MavericksGlobalMockLibrary::class.java.classLoader as BaseDexClassLoader
        val stateFlow = MutableStateFlow<Async<List<Class<out MockableMavericksView>>>>(Loading())

        GlobalScope.launch {
            getDexFiles(classLoader)
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
                }.partition { it.endsWith("Fragment") }
                .let { (possibleFragmentEntries, otherEntries) ->
                    // Prioritize checking "fragment" named classes first, since those are more likely to be matches
                    possibleFragmentEntries + otherEntries
                }
                .mapNotNull { className ->
                    getAsMavericksView(className, classLoader)
                }
                .scan(emptyList<Class<out MockableMavericksView>>()) { currentMockList, newMockableView ->
                    currentMockList + newMockableView
                }
                .onEach { stateFlow.value = Loading(it) }

            stateFlow.value = Success(stateFlow.value() ?: emptyList())
        }

        stateFlow
    }

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
    return getAsMavericksView(className, classLoader)?.let { getMockVariants(it) }
}

private fun getAsMavericksView(
    className: String,
    classLoader: ClassLoader
): Class<MockableMavericksView>? {

    val clazz = try {
        classLoader.loadClass(className)
    } catch (e: ClassNotFoundException) {
        return null
    }

    return if (MockableMavericksView::class.java.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.modifiers)) {
        @Suppress("UNCHECKED_CAST")
        clazz as Class<MockableMavericksView>
    } else {
        null
    }
}

private fun Class<*>.isMavericksView(): Boolean {
    // Similar to "isAssignableFrom", but optimized by removing non class checks to speed up mock discovery
    var cls: Class<*>? = this
    while (cls != null) {
        if (cls == mavericksClass) {
            return true
        }
        cls = cls.superclass
    }
    return false
}

private val mavericksClass = MockableMavericksView::class.java
