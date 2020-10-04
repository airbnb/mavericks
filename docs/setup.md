# Setting up Mavericks

Adding Mavericks to your app requires just two steps:

### 1. Update Your Base Fragment
Make your base Fragment class implement `MavericksView`.
***

`MavericksView` is just an interface so you can compose it with your existing base Fragment.

`MavericksView` can be implemented on any `LifecycleOwner` so you may create your own view constructs if you don't want to use Fragments.

### 2. Initialize Mavericks

In your Application onCreate method, add a single line:
```kotlin
Mavericks.initialize(this)
```

If you plan to use [Mavericks Mocking](/mocking.md), use this line instead:
```kotlin
MavericksMocks.initialize(this)
```

### [Optional] Configure Global Mavericks Plugins
Add this to your application initialization:
```kotlin
Mavericks.viewModelConfigFactory = MavericksViewModelConfigFactory(applicationContext)
```
***

You must configure the `Mavericks` object with global settings for how ViewModels should be created. The main requirement is that you set a value for `Mavericks.viewModelConfigFactory` - the `MavericksViewModelConfigFactory` specifies how ViewModels are created.

It is fine to use the default implementation of `MavericksViewModelConfigFactory`, but you must specify whether it should be created in debug mode or not. If debug mode is enabled Mavericks runs a number of [debug checks](https://github.com/airbnb/Mavericks/wiki#debug-checks) to ensure that your usage of Mavericks is correct.



This checks whether your application was built as a debuggable build, and if so will enable the debug checks.

You also may override `MavericksViewModelConfigFactory.storeContextOverride` that StateStore uses internally (see [threading](https://github.com/airbnb/Mavericks/wiki#threading-in-mvrx) and [debug-checks](https://github.com/airbnb/Mavericks/wiki#debug-checks) for more details)

#### [Optional] Configuration with Mocking Support
If you would like to take advantage of [Mavericks's mocking system](https://github.com/airbnb/Mavericks/wiki/Mavericks-Mocking-System) at all you should instead initialize the global settings via the `MavericksMocks` object in your application's initialization.
```kotlin
MavericksMocks.initialize(applicationContext)
```

This can be done _instead_ of `Mavericks.viewModelConfigFactory`, as this will set a mockable debug version of `MavericksViewModelConfigFactory` if your app was built as a debuggable build. If your app was not built debuggable (ie for production), then `MavericksMocks.initialize` will simply set up a non debug version of `MavericksViewModelConfigFactory` for you.

