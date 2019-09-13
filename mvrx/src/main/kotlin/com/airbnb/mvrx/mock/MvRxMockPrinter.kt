package com.airbnb.mvrx.mock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.mock.MvRxMockPrinter.Companion.ACTION_COPY_MVRX_STATE
import com.airbnb.mvrx.withState
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class MockPrinterConfiguration(
    /**
     * Given a [MvRxView] that we are generating mock states for, returns which package name to use
     * for the generated file.
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


/**
 * This registers a Broadcast receiver on the MvRxView (only in debug mode) that
 * listens for the intent action [ACTION_COPY_MVRX_STATE] to copy mvrx state to files on device.
 *
 * The resulting file names are printed to logcat so tooling can copy them from device.
 */
internal class MvRxMockPrinter private constructor(private val mvrxView: MvRxView) :
    LifecycleObserver {
    private val broadcastReceiver by lazy { MvRxPrintStateBroadcastReceiver(mvrxView) }
    private val context: Context
        get() {
            @Suppress("DEPRECATION")
            return when (mvrxView) {
                is View -> mvrxView.context
                is Fragment -> mvrxView.requireContext()
                is android.app.Fragment -> {
                    mvrxView.activity ?: error("Fragment context is null")
                }
                else -> error("Don't know how to get Context from mvrx view ${mvrxView.javaClass.simpleName}. Submit a PR to support your screen type.")
            }
        }

    init {
        // We don't want views to be registered multiple times, as that would result in duplicate
        // outputs. To avoid this, we track which views have been registered, and only allow one
        // at a time. We expect this to be single threaded (called from postInvalidate on the
        // main thread), so the collection is not thread safe.
        // To avoid leaking the view, it is removed when the lifecycle is destroyed.
        // If the lifecycle is already Destroyed when this is called, the observer will immediately
        // invoke the destroyed callback so the view is removed immediately as well.
        if (viewsWithRegisteredReceivers.add(mvrxView)) {
            mvrxView.lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStarted() {
        context.registerReceiver(broadcastReceiver, IntentFilter(ACTION_COPY_MVRX_STATE))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopped() {
        context.unregisterReceiver(broadcastReceiver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyed() {
        viewsWithRegisteredReceivers.remove(mvrxView)
    }

    companion object {
        const val ACTION_COPY_MVRX_STATE = "ACTION_COPY_MVRX_STATE"
        /**
         * Tracks which views already have a receiver registered, to prevent registering the same
         * view multiple times. To avoid leaks, the view is removed when it is destroyed.
         */
        private val viewsWithRegisteredReceivers = mutableSetOf<MvRxView>()

        /**
         * Register the given view to listen for broadcast receiver intents for mock state
         * printing. This is safe to call multiple times for the same [MvRxView].
         */
        fun startReceiverIfInDebug(mvrxView: MvRxView) {
            // Avoid an object allocation in the case of production
            if (MvRx.viewModelConfigProvider.debugMode) {
                MvRxMockPrinter(mvrxView)
            }
        }
    }
}

private class MvRxPrintStateBroadcastReceiver(val mvrxView: MvRxView) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(INFO_TAG, "Intent received $intent")
        if (intent.action != ACTION_COPY_MVRX_STATE) return

        // TODO: Link to script that uses these
        val viewName: String? = intent.getStringExtra("EXTRA_VIEW_NAME")
        val stateName: String? = intent.getStringExtra("EXTRA_STATE_NAME")
        val stringTruncationThreshold: Int =
            intent.getIntExtra("EXTRA_STRING_TRUNCATION_THRESHOLD", 300)
        val listTruncationThreshold: Int = intent.getIntExtra("EXTRA_LIST_TRUNCATION_THRESHOLD", 3)
        val excludeArgs = intent.getBooleanExtra("EXTRA_EXCLUDE_ARGS", false)

        if (viewName != null && !mvrxView::class.qualifiedName!!.contains(
                viewName,
                ignoreCase = true
            )
        ) {
            Log.d(INFO_TAG, "View name did not match: $viewName")
            return
        }

        Log.d(
            INFO_TAG,
            "Starting state printing. " +
                    "viewName=$viewName " +
                    "stateName=$stateName " +
                    "stringTruncationThreshold=$stringTruncationThreshold " +
                    "listTruncationThreshold=$listTruncationThreshold " +
                    "excludeArgs=$excludeArgs"
        )

        // This is done async since the reflection can be slow.
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            writeStatesForView(
                mvrxView,
                context,
                stateName,
                listTruncationThreshold,
                stringTruncationThreshold
            )

            if (!excludeArgs) {
                @Suppress("DEPRECATION")
                when (mvrxView) {
                    is Fragment -> mvrxView.arguments
                    is android.app.Fragment -> mvrxView.arguments
                    else -> {
                        Log.d(
                            ERROR_TAG,
                            "Don't know how to get arguments off of view ${mvrxView::class.qualifiedName}. " +
                                    "Only Fragments are currently supported."
                        )
                        null
                    }
                }
                    ?.get(MvRx.KEY_ARG)
                    ?.let { args ->
                        writeMock(context, args, listTruncationThreshold, stringTruncationThreshold)
                    }
            }

            // We need to pass the package name to the script so that it knows where the pull the files from.
            Log.d(RESULTS_TAG, "package=${context.applicationContext.packageName}")
            // The command line tooling watches for this "done" message to know when all states have been printed,
            // so it is important that this text doesn't change and matches exactly.
            Log.d(RESULTS_TAG, "done")
        }
    }
}

private fun writeStatesForView(
    view: MvRxView,
    context: Context,
    stateName: String?,
    listTruncationThreshold: Int,
    stringTruncationThreshold: Int
) {

    @Suppress("Detekt.TooGenericExceptionCaught")
    val viewModels = try {
        Log.d(INFO_TAG, "Looking up view models on view.")
        view.getAllViewModelsForTesting()
    } catch (e: Throwable) {
        Log.e(
            ERROR_TAG,
            "Error getting viewmodels on view ${view::class.simpleName}",
            e
        )
        emptyList<BaseMvRxViewModel<MvRxState>>()
    }

    viewModels.forEach { viewModel ->
        withState(viewModel) { state ->
            val stateClassName = state::class.qualifiedName!!
            if (stateName != null && !stateClassName.contains(stateName, ignoreCase = true)) {
                Log.d(INFO_TAG, "Ignoring state $stateClassName")
                return@withState
            }

            writeMock(context, state, listTruncationThreshold, stringTruncationThreshold)
        }
    }
}

private fun writeMock(
    context: Context,
    objectToMock: Any,
    listTruncationThreshold: Int,
    stringTruncationThreshold: Int
) {
    val objectName = objectToMock::class.simpleName

    // The reflection sometimes fails if unexpected state types are present
    @Suppress("Detekt.TooGenericExceptionCaught")
    try {
        Log.d(INFO_TAG, "Generating state for $objectName")
        printMockFile(context, objectToMock, listTruncationThreshold, stringTruncationThreshold)
    } catch (e: Throwable) {
        Log.e(ERROR_TAG, "Error creating mvrx mock code for $objectName", e)
    }
}

/**
 * Print out the code that is needed to construct the given object. This is useful for creating mock state or argument objects.
 * Use "adb logcat -s "MVRX_STATE" -v raw -v color" in the terminal to visualize the output nicely.
 *
 * @param listTruncationThreshold If greater then 0, any lists found will be truncated to this number of items. If 0 or less, no truncation will occur.
 * @param stringTruncationThreshold If greater then 0, any Strings found will be truncated to this number or characters. If 0 or less, no truncation will occur.
 */
private fun <T : Any> printMockFile(
    context: Context,
    instanceToMock: T,
    listTruncationThreshold: Int,
    stringTruncationThreshold: Int
) {
    fun Int.maxIfLTEZero() = if (this <= 0) Integer.MAX_VALUE else this

    val code = ConstructorCode(
        instanceToMock,
        listTruncationThreshold.maxIfLTEZero(),
        stringTruncationThreshold.maxIfLTEZero(),
        MvRx.mockPrinterConfiguration.customTypePrinters
    )

    val file = File(context.cacheDir, "${instanceToMock::class.simpleName}Mock.kt")

    file.printWriter().use { out ->
        out.println("package ${MvRx.mockPrinterConfiguration.mockPackage(instanceToMock)}")
        out.println()

        code.imports.forEach {
            out.println("import $it")
        }

        out.println()
        out.println(code.code)
    }

    Log.d(RESULTS_TAG, file.canonicalPath)
}

private fun MvRxView.getAllViewModelsForTesting(): List<BaseMvRxViewModel<MvRxState>> {
    return this::class.memberProperties
        .filter {
            it.returnType.isSubtypeOf(
                BaseMvRxViewModel::class.createType(
                    arguments = listOf(
                        kotlin.reflect.KTypeProjection.STAR
                    )
                )
            )
        }
        .filterIsInstance<KProperty1<MvRxView, BaseMvRxViewModel<MvRxState>>>()
        .onEach {
            it.isAccessible = true
        }
        .map {
            // The value needs to be manually retrieved from the viewmodel delegate like this for some reason, otherwise it crashes
            // saying Lazy cannot be cast to MvrxViewModel
            @Suppress("UNCHECKED_CAST")
            val delegate = it.getDelegate(this) as Lazy<BaseMvRxViewModel<MvRxState>>
            delegate.value
        }
}

/**
 * The command line tooling looks for output with these tags - so it is important that these are not changed
 * without also updating that script.
 */
private const val RESULTS_TAG = "MVRX_PRINTER_RESULTS"
private const val ERROR_TAG = "MVRX_PRINTER_ERROR"
private const val INFO_TAG = "MVRX_PRINTER_INFO"
