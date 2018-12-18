
package com.airbnb.mvrx

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelStore
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.airbnb.mvrx.MvRxViewModelProvider.createViewModel
import kotlin.collections.set

/**
 * Custom ViewModelStore that supports persisting and restoring ViewModels.
 *
 * A [android.arch.lifecycle.ViewModelStoreOwner] such as a [android.support.v4.app.Fragment] or [android.support.v4.app.FragmentActivity]
 * should override their getViewModelStore function and return an instance of this class.
 *
 * Then, to support persistence across processes, you must:
 * Save: Call [restoreViewModels] from *before onCreate* and [saveViewModels] in onSaveInstanceState.
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

    private var restoreViewModelsCalled = false

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
                    val holder = MvRxPersistedViewModelHolder(viewModel::class.java, state::class.java, persistedState)
                    bundle.apply { putParcelable(key, holder) }
                }
            }
            .let {
                outState.putBundle(KEY_MVRX_SAVED_INSTANCE_STATE, it)
                outState.putSerializable(KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS, fragmentArgsForActivityViewModelState)
            }
    }

    /**
     * Iterates through all persisted ViewModels saved with [saveViewModels] and stores them in the store map.
     *
     * This should be called from [android.support.v4.app.Fragment.onCreate] or
     * [android.support.v4.app.FragmentActivity.onCreate].
     */
    fun restoreViewModels(activity: FragmentActivity, savedInstanceState: Bundle?) {
        restoreViewModelsCalled = true
        savedInstanceState ?: return
        val args = activity.intent.extras?.get(MvRx.KEY_ARG)
        restoreViewModels(map, activity, savedInstanceState, args)
    }

    fun restoreViewModels(fragment: Fragment, savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        val args = fragment.arguments?.get(MvRx.KEY_ARG)
        restoreViewModels(map, fragment, savedInstanceState, args)
    }

    internal fun restoreViewModels(map: MutableMap<String, ViewModel>, activity: FragmentActivity, savedInstanceState: Bundle?, ownerArgs: Any? = null) {
        restoreViewModels(activity, map, savedInstanceState, ownerArgs)
    }

    internal fun restoreViewModels(map: MutableMap<String, ViewModel>, fragment: Fragment, savedInstanceState: Bundle?, ownerArgs: Any? = null) {
        restoreViewModels(fragment, map, savedInstanceState, ownerArgs)
    }

    private fun <H> restoreViewModels(host: H, map: MutableMap<String, ViewModel>, savedInstanceState: Bundle?, ownerArgs: Any? = null) {
        savedInstanceState ?: return
        val viewModelsState = savedInstanceState.getBundle(KEY_MVRX_SAVED_INSTANCE_STATE)
            ?: throw IllegalStateException("You are trying to call restoreViewModels but you never called saveViewModels!")
        restoreFragmentArgsFromSavedInstanceState(savedInstanceState)
        if (map.isNotEmpty()) return
        viewModelsState.keySet()?.forEach {
            // In the case that we are restoring an Activity ViewModel created by a Fragment, `ownerArgs` will be those of the Activity. So we
            // need to use the persisted Fragment args instead.
            val arguments = if (fragmentArgsForActivityViewModelState.containsKey(it)) {
                fragmentArgsForActivityViewModelState[it]
            } else {
                ownerArgs
            }
            map[it] = (host as? Fragment)
                    ?.let { fragment -> restoreViewModel(fragment, viewModelsState.getParcelable(it), arguments) }
                    ?: restoreViewModel(host as FragmentActivity, viewModelsState.getParcelable(it), arguments)
        }
    }

    private fun restoreFragmentArgsFromSavedInstanceState(savedInstanceState: Bundle) {
        // Re-populate all persisted fragment args.
        @Suppress("UNCHECKED_CAST")
        (savedInstanceState.get(KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS) as? HashMap<String, Any?>)
            ?.let { fragmentArgsForActivityViewModelState.putAll(it) }
    }

    private fun restoreViewModel(activity: FragmentActivity, holder: MvRxPersistedViewModelHolder, arguments: Any?): ViewModel {
        val (viewModelClass, stateClass, viewModelState) = holder
        // If there is a key in the fragmentArgsForActivityViewModelState map, then this is an activity ViewModel. The map value will contain
        // the Fragment args that the ViewModel was created with.
        val state = _initialStateProvider(stateClass, arguments).let(viewModelState::restorePersistedState)
        return createViewModel(viewModelClass, activity, state)
    }

    private fun restoreViewModel(fragment: Fragment, holder: MvRxPersistedViewModelHolder, arguments: Any?): ViewModel {
        val (viewModelClass, stateClass, viewModelState) = holder
        // If there is a key in the fragmentArgsForActivityViewModelState map, then this is an activity ViewModel. The map value will contain
        // the Fragment args that the ViewModel was created with.
        val state = _initialStateProvider(stateClass, arguments).let(viewModelState::restorePersistedState)
        return createViewModel(viewModelClass, fragment, state)
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
        if (!restoreViewModelsCalled) throw IllegalStateException("You must call restoreInstanceState on your MvRxViewModelStore!")
        fragmentArgsForActivityViewModelState[key] = args
    }

    companion object {
        private const val KEY_MVRX_SAVED_INSTANCE_STATE = "mvrx:saved_instance_state"
        private const val KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS = "mvrx:activity_scoped_fragment_args"

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
