package com.airbnb.mvrx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.MvRxViewModelProvider.createViewModel
import kotlin.collections.set

/**
 * Custom ViewModelStore that supports persisting and restoring ViewModels.
 *
 * A [androidx.lifecycle.ViewModelStoreOwner] such as a [androidx.fragment.app.Fragment] or [androidx.fragment.app.FragmentActivity]
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
     * This should be called from [androidx.fragment.app.Fragment.onSaveInstanceState] or
     * [androidx.fragment.app.FragmentActivity.onSaveInstanceState].
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
            .let { bundle ->
                outState.putBundle(KEY_MVRX_SAVED_INSTANCE_STATE, bundle)
                outState.putSerializable(KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS, fragmentArgsForActivityViewModelState)
            }
    }

    /**
     * Iterates through all persisted ViewModels saved with [saveViewModels] and stores them in the store map.
     *
     * This should be called from [androidx.fragment.app.Fragment.onCreate] or
     * [androidx.fragment.app.FragmentActivity.onCreate].
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
        viewModelsState.keySet()?.forEach { key ->
            // In the case that we are restoring an Activity ViewModel created by a Fragment, `ownerArgs` will be those of the Activity. So we
            // need to use the persisted Fragment args instead.
            val arguments = if (fragmentArgsForActivityViewModelState.containsKey(key)) {
                fragmentArgsForActivityViewModelState[key]
            } else {
                ownerArgs
            }
            val holder = viewModelsState.getParcelable<MvRxPersistedViewModelHolder>(key)
                ?: throw IllegalStateException("ViewModel key: $key expected to be in stored ViewModels but was not found.")
            map[key] = when (host) {
                is Fragment -> restoreViewModel(key, host, holder, arguments)
                is FragmentActivity -> restoreViewModel(key, host, holder, arguments)
                else -> throw IllegalStateException("Host: $host is expected to be either Fragment or FragmentActivity.")
            }
        }
    }

    private fun restoreFragmentArgsFromSavedInstanceState(savedInstanceState: Bundle) {
        // Re-populate all persisted fragment args.
        @Suppress("UNCHECKED_CAST")
        (savedInstanceState.get(KEY_MVRX_ACTIVITY_SCOPED_FRAGMENT_ARGS) as? HashMap<String, Any?>)
            ?.let { fragmentArgsForActivityViewModelState.putAll(it) }
    }

    private fun restoreViewModel(key: String, activity: FragmentActivity, holder: MvRxPersistedViewModelHolder, arguments: Any?): ViewModel {
        val (viewModelClass, stateClass, viewModelState) = holder
        return createViewModel(key, viewModelClass, stateClass, ActivityViewModelContext(activity, arguments), viewModelState::restorePersistedState)
    }

    private fun restoreViewModel(key: String, fragment: Fragment, holder: MvRxPersistedViewModelHolder, arguments: Any?): ViewModel {
        val (viewModelClass, stateClass, viewModelState) = holder
        return createViewModel(key, viewModelClass, stateClass, FragmentViewModelContext(fragment.requireActivity(), arguments, fragment), viewModelState::restorePersistedState)
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
