package com.airbnb.mvrx

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelStore
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.airbnb.mvrx.MvRxViewModelProvider.createDefaultViewModel
import java.util.UUID
import kotlin.reflect.full.companionObjectInstance

/**
 * Custom ViewModelStore that supports persisting and restoring ViewModels.
 *
 * A [android.arch.lifecycle.ViewModelStoreOwner] such as a [android.support.v4.app.Fragment] or [android.support.v4.app.FragmentActivity]
 * should override their getViewModelStore function and return an instance of this class.
 *
 * Then, to support persistence across processes, you must:
 * Save: Call [saveViewModels] from the same method in your StoreOwner.
 *
 * Restore: Call [restoreViewModels] from onCreate in an Activity or onActivityCreated in a Fragment. It must be in onActivityCreated
 * beacuse it must be attached to an activity.
 *
 * @see MvRxViewModelFactory
 */
class MvRxViewModelStore(private val viewModelStore: ViewModelStore) {

    /**
     * Returns the map of created ViewModels.
     */
    @Suppress("UNCHECKED_CAST")
    private val map: HashMap<String, ViewModel> by lazy { mMapMethod.get(viewModelStore)!! as HashMap<String, ViewModel> }

    /**
     * In a new process, the _Activity_ will restore Activity ViewModels created by Fragments, not the Fragment which created the ViewModel.
     * This map stores the required Fragment arguments, added via [_saveActivityViewModelArgs].
     */
    private val fragmentArgsForActivityViewModelState = HashMap<String, Any?>()

    /**
     * Iterates through all ViewModels, persists its current state with [PersistState] and stores it in outState.
     *
     * This should be called from [android.support.v4.app.Fragment.onSaveInstanceState] or
     * [android.support.v4.app.FragmentActivity.onSaveInstanceState].
     */
    fun saveViewModels(outState: Bundle) {
        saveViewModels(map, outState)
    }

    @Suppress("UNCHECKED_CAST")
    fun saveViewModels(map: MutableMap<String, out ViewModel>, outState: Bundle) {
        map.entries
            .filter { it.value as? BaseMvRxViewModel<MvRxState> != null }
            .map { it.key to it.value as BaseMvRxViewModel<MvRxState> }
            .fold(Bundle()) { bundle, (key, viewModel) ->
                withState(viewModel) { state ->
                    val persistedState = state.persistState()
                    val holder = MvRxPersistedViewModelHolder(viewModel::class.java.name, state::class.java.name, persistedState)
                    bundle.apply { putParcelable(key, holder) }
                }
            }
            .let {
                outState.putBundle(KEY_MVRX_SAVED_INSTANCE_STATE, it)
                outState.putString(KEY_MVRX_PROCESS_UUID, processUuid)
                outState.putSerializable(KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS, fragmentArgsForActivityViewModelState)
            }
    }

    /**
     * Iterates through all persisted ViewModels saved with [saveViewModels] and stores them in the store map.
     *
     * This should be called from [android.support.v4.app.Fragment.onActivityCreated] or
     * [android.support.v4.app.FragmentActivity.onCreate].
     */
    fun restoreViewModels(activity: FragmentActivity, savedInstanceState: Bundle?) {
        val args = activity.intent.extras?.get(MvRx.KEY_ARG)
        restoreViewModels(map, activity, savedInstanceState, args)
    }

    fun restoreViewModels(fragment: Fragment, savedInstanceState: Bundle?) {
        val args = fragment.arguments?.get(MvRx.KEY_ARG)
        restoreViewModels(map, fragment.requireActivity(), savedInstanceState, args)
    }

    fun restoreViewModels(map: MutableMap<String, ViewModel>, activity: FragmentActivity, savedInstanceState: Bundle?, ownerArgs: Any? = null) {
        savedInstanceState ?: return
        val viewModelsState = savedInstanceState.getBundle(KEY_MVRX_SAVED_INSTANCE_STATE)
        val uuid = savedInstanceState.getString(KEY_MVRX_PROCESS_UUID)
        // No need to restore state if we are in the same process
        if (processUuid == uuid) return
        restoreFragmentArgsFromSavedInstanceState(savedInstanceState)
        viewModelsState?.keySet()?.forEach {
            // In the case that we are restoring an Activity ViewModel created by a Fragment, `ownerArgs` will be those of the Activity. So we
            // need to use the persisted Fragment args instead.
            val arguments = if (fragmentArgsForActivityViewModelState.containsKey(it)) {
                fragmentArgsForActivityViewModelState[it]
            } else {
                ownerArgs
            }
            map[it] = restoreViewModel(activity, viewModelsState.getParcelable(it), arguments)
        }
    }

    private fun restoreFragmentArgsFromSavedInstanceState(savedInstanceState: Bundle) {
        // Re-populate all persisted fragment args.
        @Suppress("UNCHECKED_CAST")
        (savedInstanceState.get(KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS) as? HashMap<String, Any?>)
            ?.let { fragmentArgsForActivityViewModelState.putAll(it) }
    }

    private fun restoreViewModel(activity: FragmentActivity, holder: MvRxPersistedViewModelHolder, arguments: Any?): ViewModel {
        val (viewModelClassName, stateClassName, viewModelState) = holder
        @Suppress("UNCHECKED_CAST")
        val viewModelClass = (Class.forName(viewModelClassName) as Class<BaseMvRxViewModel<MvRxState>>).kotlin
        @Suppress("UNCHECKED_CAST")
        val stateClass = Class.forName(stateClassName) as Class<MvRxState>
        // If there is a key in the fragmentArgsForActivityViewModelState map, then this is an activity ViewModel. The map value will contain
        // the Fragment args that the ViewModel was created with.
        val state =
            try {
                _initialStateProvider(stateClass.kotlin, arguments).let(viewModelState::restorePersistedState)
            } catch (exception: IllegalStateException) {
                // Todo @ben.schwab the update to state creation broke some existing MvRx flows that did not have a 0 arg default constructor.
                // We should fix all screens ASAP, but for now in the case of an exception, fall back to the legacy manner of state creation.
                viewModelState.createInitialStateFromPersistedState(stateClass)
            }

        @Suppress("UNCHECKED_CAST")
        val factory = viewModelClass.companionObjectInstance as? MvRxViewModelFactory<MvRxState>
        return factory?.let { factory.create(activity, state) } ?: createDefaultViewModel(viewModelClass, state)
    }

    /**
     * Visible for inline. When a Fragment creates a ViewModel scoped to an Activity, the initial state provider calls this method so that the
     * Fragment args are available from the Activity when the ViewModel is recreated in a new process.
     *
     * @param key The same key used to identity the view model.
     * @param args The MvRx args from the fragment creating this activity ViewModel.
     */
    @Suppress("FunctionName")
    fun _saveActivityViewModelArgs(key: String, args: Any?) {
        fragmentArgsForActivityViewModelState[key] = args
    }

    companion object {
        private const val KEY_MVRX_SAVED_INSTANCE_STATE = "mvrx:saved_instance_state"
        private const val KEY_MVRX_PROCESS_UUID = "mvrx:process_uuid"
        private const val KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS = "mvrx:activity_scoped_fragment_args"
        private val processUuid = UUID.randomUUID().toString()

        /**
         * mMap is private in ViewModelStore but we need to access it to save state.
         */
        private val mMapMethod by lazy {
            val field = ViewModelStore::class.java.getDeclaredField("mMap")
            field.isAccessible = true
            field
        }
    }
}
