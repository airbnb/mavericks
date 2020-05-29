# Debug Checks

MvRx has a debug mode that enables some validation checks. When you integrate MvRx into your app, you will need to create your own MvRxViewModel that extends BaseMvRxViewModel. One of the required things to override is protected abstract val debugMode: Boolean. You could set it to something like BuildConfig.DEBUG. The checks include:

* Running all state reducers twice and ensuring that the resulting state is the same. This ensures that reducers are pure.
* All state properties are immutable vals not vars.
* State types are not one of: ArrayList, SparseArray, LongSparseArray, SparseArrayCompat, ArrayMap, and HashMap.
* State class visibility is public so it can be created and restored
* Each instance of your State is not mutated once it is set on the viewmodel
