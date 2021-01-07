# Debug Checks

Mavericks has a debug mode that enables some validation checks. These checks will safeguard against a number of common mistakes or other issues that could cause rare and hard to debug issues for real world users.

The checks include:

* Running all state reducers twice and ensuring that the resulting state is the same. This ensures that reducers are pure.
* All state properties are immutable vals not vars.
* State types are not one of: ArrayList, SparseArray, LongSparseArray, SparseArrayCompat, ArrayMap, and HashMap.
* State class visibility is public so it can be created and restored
* Each instance of your State is not mutated once it is set on the ViewModel

### Enabling debug mode

When you [integrate Mavericks into your app](/setup), there are overloads to pass in `Context` or to explicitly set `debugMode`. If you initialize Mavericks with `Context` (recommended) then debug mode will automatically be enabled if your application is debuggable (`context.applicationInfo.flags` contains `ApplicationInfo.FLAG_DEBUGGABLE`).


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
