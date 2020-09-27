# Debug Checks

MvRx has a debug mode that enables some validation checks. When you integrate MvRx into your app, you will need to create your own MvRxViewModel that extends BaseMvRxViewModel. One of the required things to override is protected abstract val debugMode: Boolean. You could set it to something like BuildConfig.DEBUG. The checks include:

* Running all state reducers twice and ensuring that the resulting state is the same. This ensures that reducers are pure.
* All state properties are immutable vals not vars.
* State types are not one of: ArrayList, SparseArray, LongSparseArray, SparseArrayCompat, ArrayMap, and HashMap.
* State class visibility is public so it can be created and restored
* Each instance of your State is not mutated once it is set on the viewmodel

### Avoid slow operations in `withState`/`setState`
As all `withState`/`setState` blocks are processed sequentially, it's highly recommended to avoid slow calls inside these blocks (blocking IO/cpu consuming calls). You can use Android [StrictMode](https://developer.android.com/reference/android/os/StrictMode) to detect such calls. StrictMode.ThreadPolicy can be injected into `MavericksViewModelConfigFactory.storeContextOverride`:
```
val threadPolicy = StrictMode.ThreadPolicy.Builder()
    .detectNetwork()
    .penaltyDialog()
    .build()

Mavericks.viewModelConfigFactory = MavericksViewModelConfigFactory(
    this,
    storeContextOverride = if (isDebuggable()) threadPolicy.asContextElement() else EmptyCoroutineContext
)
```
