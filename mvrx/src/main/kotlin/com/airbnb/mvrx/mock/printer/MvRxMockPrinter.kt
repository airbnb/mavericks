package com.airbnb.mvrx.mock.printer

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
import com.airbnb.mvrx.mock.MvRxMocks
import com.airbnb.mvrx.mock.printer.MvRxMockPrinter.Companion.ACTION_COPY_MVRX_STATE
import com.airbnb.mvrx.withState
import java.io.File


/**
 * This registers a Broadcast receiver on the MvRxView (only in debug mode) that
 * listens for the intent action [ACTION_COPY_MVRX_STATE] to copy mvrx state to files on device.
 *
 * The resulting file names are printed to logcat so tooling can copy them from device.
 */
internal class MvRxMockPrinter private constructor(
    private val mvrxView: MvRxView
) : LifecycleObserver {

    private val broadcastReceiver by lazy { ViewArgPrinter(mvrxView) }
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
        broadcastReceiver.register(context)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopped() {
        broadcastReceiver.unregister(context)
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
         * printing. This is safe to call multiple times for the same [MvRxView], and is a no-op
         * if not in debug mode.
         */
        fun startReceiver(mvrxView: MvRxView) {
            if (MvRx.nonNullViewModelConfigFactory.debugMode) {
                MvRxMockPrinter(mvrxView)
            }
        }
    }
}

/**
 * When an Intent with the [ACTION_COPY_MVRX_STATE] action is received, this class will:
 *
 * 1. Extract configuration arguments from the Intent
 * 2. Lookup all ViewModel properties defined on the [MvRxView]
 * 3. Get the current State of each ViewModel
 * 4. Use [ConstructorCode] to generate the Kotlin code needed to reconstruct the States
 * 5. Saves the code to files on the device.
 * 6. Uses logcat to signal where on device the files were saved, and when the process is done
 * 7. A listener can wait for the output signals, and then pull the resulting files off the device.
 */
abstract class MvRxPrintStateBroadcastReceiver : BroadcastReceiver() {

    private var isRegistered: Boolean = false

    data class Settings(
        val includeRegexes: List<Regex>,
        val excludeRegexes: List<Regex>,
        val stringTruncationThreshold: Int,
        val listTruncationThreshold: Int
    )

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(INFO_TAG, "$tag - Intent received $intent")
        if (intent.action != ACTION_COPY_MVRX_STATE) {
            Log.d(INFO_TAG, "$tag - Unsupported action: ${intent.action}")
            return
        }
        // The script looks for "started" and "done messages to know when work is done. If multiple Fragments are started then
        // it needs to know how many are still working, so it tracks how many are not yet done.
        Log.d(RESULTS_TAG, "started")

        fun String?.parseRegexes(): List<Regex> {
            if (this == null) return emptyList()
            return split(",").filter { it.isNotBlank() }.map { Regex(it) }
        }

        // These string extra names are defined in the mock printer kts script, and
        // they allow for configuration in how the mock state is gathered and printed.
        val settings = Settings(
            includeRegexes = intent.getStringExtra("EXTRA_INCLUDE_REGEXES").parseRegexes(),
            excludeRegexes = intent.getStringExtra("EXTRA_EXCLUDE_REGEXES").parseRegexes(),
            stringTruncationThreshold = intent.getIntExtra(
                "EXTRA_STRING_TRUNCATION_THRESHOLD",
                300
            ),
            listTruncationThreshold = intent.getIntExtra("EXTRA_LIST_TRUNCATION_THRESHOLD", 3)
        )


        // This is done async since the mock generation reflection can be slow.
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            if (isMatch(settings)) {
                val objectToMock = provideObjectToMock()
                if (objectToMock == null) {
                    Log.d(INFO_TAG, "$tag - No object to mock")
                } else {
                    Log.d(
                        INFO_TAG,
                        "$tag - Starting state printing for ${objectToMock.javaClass.simpleName}. $settings"
                    )
                    writeMock(
                        context,
                        objectToMock,
                        settings.listTruncationThreshold,
                        settings.stringTruncationThreshold
                    )
                }
            } else {
                Log.d(INFO_TAG, "$tag - did not match intent target. $settings")
                // Continue onward so we still report "done", so that the script isn't left hanging
                // waiting for it.
            }

            // We need to pass the package name to the script so that it knows where to pull the files from.
            Log.d(RESULTS_TAG, "package=${context.applicationContext.packageName}")
            // The command line tooling watches for this "done" message to know when all states have been printed,
            // so it is important that this text doesn't change and matches exactly.
            // We delay briefly in case there are other fragments that are printing state - we need to make  sure they have all had
            // time to print their "started" message so that the script knows they are still working.
            Thread.sleep(1000)
            Log.d(RESULTS_TAG, "done")
        }
    }

    private fun isMatch(settings: Settings): Boolean {
        val names = objectsToCheckForNameMatch.mapNotNull {
            it?.javaClass?.canonicalName
        }

        if (names.isEmpty()) return false

        if (names.any { name -> settings.excludeRegexes.any { it.containsMatchIn(name) } }) {
            return false
        }

        if (settings.includeRegexes.isEmpty()) {
            return true
        }

        return names.any { name -> settings.includeRegexes.any { it.containsMatchIn(name) } }
    }

    protected open val objectsToCheckForNameMatch: List<Any?> get() = listOf(provideObjectToMock())

    abstract fun provideObjectToMock(): Any?

    abstract val tag: String

    fun register(context: Context) {
        check(!isRegistered) { "Already registered" }
        isRegistered = true
        context.registerReceiver(this, IntentFilter(ACTION_COPY_MVRX_STATE))
    }

    fun unregister(context: Context) {
        check(isRegistered) { "Not registered" }
        isRegistered = false
        context.unregisterReceiver(this)
    }
}

/**
 * An instance of [MvRxPrintStateBroadcastReceiver] that prints the arguments of the given view.
 */
private class ViewArgPrinter(val mvrxView: MvRxView) : MvRxPrintStateBroadcastReceiver() {

    override fun provideObjectToMock(): Any? {
        @Suppress("DEPRECATION")
        val argsBundle = when (mvrxView) {
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

        return argsBundle?.get(MvRx.KEY_ARG)
    }

    //  Include arguments in mock printing if either the arguments or Fragment match the name filters
    override val objectsToCheckForNameMatch: List<Any?>
        get() = listOf(mvrxView, provideObjectToMock())

    override val tag: String = mvrxView.javaClass.simpleName
}

/**
 * An instance of [MvRxPrintStateBroadcastReceiver] that prints the state of the given view model.
 */
internal class ViewModelStatePrinter<S : MvRxState>(
    val viewModel: BaseMvRxViewModel<S>
) : MvRxPrintStateBroadcastReceiver() {

    private fun currentState(): S {
        return withState(viewModel) { it }
    }

    override fun provideObjectToMock(): Any? = currentState()

    //  Include state in mock printing if either the state or view model match the name filters
    override val objectsToCheckForNameMatch: List<Any?>
        get() = listOf(viewModel, provideObjectToMock())

    override val tag: String = viewModel.javaClass.simpleName
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
        printMockFile(
            context,
            objectToMock,
            listTruncationThreshold,
            stringTruncationThreshold
        )
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
        MvRxMocks.mockPrinterConfiguration.customTypePrinters
    )

    val file = File(context.cacheDir, "${instanceToMock::class.simpleName}Mock.kt")

    file.printWriter().use { out ->
        out.println("package ${MvRxMocks.mockPrinterConfiguration.mockPackage(instanceToMock)}")
        out.println()

        code.imports.forEach {
            out.println("import $it")
        }

        out.println()
        out.println(code.lazyPropertyToCreateObject)
    }

    Log.d(RESULTS_TAG, file.canonicalPath)
}

/**
 * The command line tooling looks for output with these tags - so it is important that these are not changed
 * without also updating that script.
 */
private const val RESULTS_TAG = "MVRX_PRINTER_RESULTS"
private const val ERROR_TAG = "MVRX_PRINTER_ERROR"
private const val INFO_TAG = "MVRX_PRINTER_INFO"
