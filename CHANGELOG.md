# Change Log

For a full list of changes, see the [GitHub releases](https://github.com/airbnb/mavericks/releases)

## 3.0.9
- Fix crash in state restoration with multiple of 32 fields (#707)

## 3.0.3
- Changed flowWhenStarted to emit value on flow coroutine context (#678)

## 3.0.2
- Wrap subscription action with lifecycle whenStarted (#665)
- Updated embedded Proguard/r8 rules to work with R8 full mode (#658)

## 3.0.1
- Fix issue where `mavericks-common` module was not published to maven in 3.0.0

## 3.0.0
- Experimental `mavericks-common` module with new abstraction `MavericksRepository` that behaves exactly like `MavericksViewModel` except it doesn't have any Android dependencies and can be used in pure Kotlin modules (#635)
- Breaking changes: `MavericksViewModelConfig.BlockExecutions` is extracted into top level class `MavericksBlockExecutions` (#635)
- New mavericks extension `argsOrNull` to handle optional (nullable) fragment args (#639)
- New Anvil sample in the `sample-anvil` module

## 2.7.0
- Add mockEightViewModels and mockNineViewModels to MockBuilder (#633)

## 2.6.1
- Expose state restoration functions (#611)
- Use passed scope as fragment if possible (#618)

## 2.6.0
Big thanks to @itsandreramon for contributing the two main improvements in this release!

- Pre-configure Hilt by adding a new "mvrx-hilt" artifact (#598)

See the updated Hilt documentation for guidance on how to more easily use Hilt with Mavericks https://airbnb.io/mavericks/#/dagger?id=hilt

- Add support to use Mavericks with JUnit 5 (#600)

See the new testing documentation at https://airbnb.io/mavericks/#/testing for details.

- Don't expose lifecycleAwareLazy in viewModel return type (#603)


## 2.5.1
- Add ability to manually pass argument to composable viewModel factories (#595)
- Fix Fragment arguments not being correctly passed to viewmodel state initialization in compose usage (#595)
- Switch mavericks-compose artifact to use same versioning scheme as other artifacts

## 2.5.0
- Fix issue when the LocalContext is not directly an Activity (#582)
- update to Compose 1.0.4, Kotlin 1.5.31, Koin 3.1.3 (#586)
- Ignore VerifyError Exception when loading potential mockable classes #590

## 2.4.0
- Add covariant recreation support (#565)
- Exposing unique subscription handling for custom flow operations (#560)
- Add support for restoring ViewModels that were initially created with a companion factory in a superclass #566

## 2.3.0
- Error handling enhancements (#540)
- Upgraded Compose to beta07 (#549)

Note: Compose support is experimental and mvrx-compose artifact is at version 2.1.0-alpha02

## 2.2.0
- Fix subscriptionLifecycleOwner to use viewLifecycleOwner in Fragment's onCreateView (#533)
- Remove createUnsafe and don't auto-subscribe on background threads (#525)
- Fix lifecycle 2.3.0 throwing IllegalStateException when using `MavericksLauncherActivity` (#523)

## 2.1.0
- Upgraded to Kotlin 1.4.30.
- Removed `@RestrictTo` annotations in favor of just `@InternalMavericksApi`. The Kotlin opt-in annotations work more reliably than the Android lint rules and there is no need for both.
- Created initial release of [mavericks-compose](https://airbnb.io/mavericks/#/jetpack-compose).

### Breaking Changes
- ActivityViewModelContext and MavericksViewModelFactory now uses ComponentActivity instead of FragmentActivity to improve Compose interop. ComponentActivity is the super class of FragmentActivity so you may need to replace FragmentActivity with ComponentActivity if you using ActivityViewModelContext.

## Version 2.0.0
Mavericks 2.0 is a ground up rewrite for coroutines. Check out the [documentation for 2.0](https://airbnb.io/mavericks/#/new-2x) to find out what is new and how to upgrade.

### Breaking Changes
- All `mvrx` artifact names are now `mavericks`.
- If you are using RxJava, you will need to use `mavericks-rxjava2` to maintain backwards compatibility. New Mavericks users who just use coroutines can just use `mavericks`.
- If your MavericksView/Fragment does not use any ViewModels, invalidate() will NOT be called in onStart(). In MvRx 1.x, invalidate would be called even if MvRx was not used at all. If you would like to maintain the original behavior, call `postInvalidate()` from onStart in your base Fragment class
- MavericksViewModel and BaseMvRxViewModel (from mavericks-rxjava2) no longer extends Jetpack ViewModel. However, `viewModelScope` and `onCleared()` still exist to match the existing API
- The order of nested with and set states has changed slightly. It now matches the original intention.
If you had code like:
```kotlin
withState {
    // W1
    withState {
        // W2
    }
    setState {
        // S1
        setState {
            // S2
            setState {
                // S3
            }
        }
    }
}
```
Previously, setState would only be prioritized at that nesting level so it would run:
[W1, S1, W2, S2, S3]
Now, it will run:
[W1, S1, S2, S3, W2]
- viewModelScope is now a property on MavericksViewModel and BaseMvRxViewModel (from mavericks-rxjava2), not the Jetpack extension function for ViewModel. Functionally, this is the same but the previous viewModelScope import will now be unused
- If you had been using any restricted internal mavericks library functions your build may break as they have been renamed (you shouldn't be doing this, but in case you are...)

### Other Changes
- Make MavericksViewModel extension functions protected (#488)
- Add MavericksViewModel.awaitState (#487) to access current ViewModel state via a suspend function
- Mark all @RestrictTo APIs with @InternalMavericksApi (#480)
- Api additions to the mocking framework (#475) (#477)
- Migrated CoroutinesStateStore to SharedFlow (#469)
- Launcher and mock speed optimizations (#468)
- FragmentViewModelContext now allows for custom ViewModelStoreOwner and/or SavedStateRegistry that are different from the fragment ones in FragmentViewModelContext. (#443)
- Add mavericks-navigation artifact to support AndroidX Navigation destination ViewModels `navGraphViewModel(R.id.my_graph)` (#443)

## Version 1.5.1
- Fix incorrectly failing debug assertions for state class being a data class when a property has internal visibility

## Version 1.5.0
- Add an optional nullable value to all Async classes (#383)
- Update various dependencies

Note: MvRx now targets 1.8 for Java/Kotlin, so you may need to update your projects to also target
1.8
```
android {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
```

## Version 1.4.0
- Remove Kotlin-Reflect entirely (#334)
- Remove extra proguard dependency (#310)

## Version 1.3.0
- Revamp state saving to use Android Jetpack `SavedStateRegistry` (#254)

Breaking - This removes the need for `MvRxViewModelStoreOwner` and `BaseMvRxActivity` and thus those classes
are now deleted.

If you were using `BaseMvRxActivity` you can instead now extend from `AppCompatActivity`.

All usages of `mvrxViewModelStore` can simply be deleted - state saving now happens completley
automatically.

## Version 1.2.1
- Fix sources not included in 1.2.0 release

## Version 1.2.0
- Make searching for the companion object factory more robust #283
- Fix infinite thread leaks while flushing queues in state store #302
- Adding inter view model subscription support #287

## Version 1.1.0
- New `parentFragmentViewModel()` and `targetFragmentViewModel()` ViewModel delegate scopes [#247](https://github.com/airbnb/MvRx/pull/247)
- Disallow functions in state [#250](https://github.com/airbnb/MvRx/pull/250)
- Log execute errors in debug mode [#260](https://github.com/airbnb/MvRx/pull/260)
- Fixed proguard issues with 1.0.2 [#267](https://github.com/airbnb/MvRx/pull/267)

## Version 1.0.2
- Synchronized and improved Kotlin cache warming [#244](https://github.com/airbnb/MvRx/pull/244)
- Made flushQueues tailrec to improve performance [#252](https://github.com/airbnb/MvRx/pull/252)
- Updated RxJava to 2.1.1 and RxAndroid to 2.2.8 [#233](https://github.com/airbnb/MvRx/pull/233)

## Version 1.0.1
- MvRxTestRule can now disable lifecycle checking for subscriptions in tests [#235](https://github.com/airbnb/MvRx/pull/235)
- Improve the error message for impure reducers [#229](https://github.com/airbnb/MvRx/pull/229)
- Allow Fail to be used with exceptions that have no stack trace [#225](https://github.com/airbnb/MvRx/pull/235)

## Version 1.0.0
- MvRx now requires androidx.
- Fix: prevented duplicate subscriptions when subscriptions are made after onCreate. [#210](https://github.com/airbnb/MvRx/pull/210)
- Respect uniqueOnly across configuration changes. [#207](https://github.com/airbnb/MvRx/pull/207)
  - This comes with additional code in BaseMvRxFragment. If you are not using BaseMvRxFragment, please refer to [#207](https://github.com/airbnb/MvRx/pull/207) for the additional required code.
- Remove state coalescing. [#206](https://github.com/airbnb/MvRx/pull/206)
- Require that all MvRxViewModels actually provide a value for debugMode. [#196](https://github.com/airbnb/MvRx/pull/196)

## Version 0.8.0
- **BREAKING:** `BaseMvRxViewModel` now requires the debug flag be explicitly set [#196](https://github.com/airbnb/MvRx/pull/196) 

## Version 0.7.2
*(2019-1-16)*
- Fix: mvrx-testing artifact now has the correct group name [#184](https://github.com/airbnb/MvRx/pull/184)

## Version 0.7.1
*(2019-1-14)*
- New: Add app() to ViewModelFactoryContext. [#179](https://github.com/airbnb/MvRx/pull/179)
- Fix: Prevent private state properties from crashing debug checks. [#178](https://github.com/airbnb/MvRx/pull/178)
- Fix: Fix proguard docs and rules for new ViewModelFactory APIs. [#181](https://github.com/airbnb/MvRx/pull/181)

## Version 0.7.0
*(2019-1-11)*
- **BREAKING:** `MvRxViewModelFactory` has been updated to provide more flexiblity when creating ViewModel and initial state [#169](https://github.com/airbnb/MvRx/pull/169): 
  - Type signature changed from `MvRxFactory<S>` to `MvRxFactory<VM, S>` for better type safety.
  - Changed signature of `MvRxViewModelFactory#create(activity: FragmentActivity, state: S) : VM` to `MvRxViewModelFactory#create(viewModelContext: ViewModelContext, state: S) : VM?`. `ViewModelContext` contains a reference to the owning activity and MvRx fragment arguments. For fragment scoped ViewModels, it will be of type `FragmentViewModelContext` with a reference to the creating fragment. These changes allow depedency injection of your ViewModel.
  - New: Added `MvRxViewModelFactory#initialState(viewModelContext: ViewModelContext) : S?`. This is useful if the initial state requires information not available in `args`.
  - New: `@JvmStatic` no longer required on `MvRxViewModelFactory#create` and new `MvRxViewModelFactory#initialState`.
- Migration guide, required changes marked with `**`:
```kotlin
    companion object : MvRxViewModelFactory<**RandomDadJokeViewModel**, RandomDadJokeState> { 

        ** // No JvmStatic needed **
        override fun create(viewModelContext: ViewModelContext, state: RandomDadJokeState): **RandomDadJokeViewModel** {
            val service: DadJokeService by **viewModelContext.activity**.inject()
            return RandomDadJokeViewModel(state, service)
        }
    }
```
- New: [#154](https://github.com/airbnb/MvRx/pull/154) Created `MvRxTestRule` in `mvrx-testing` artifact to disable debug checks and change RxSchedulers. Add the test library:
```
dependencies {
  testImplementation 'com.airbnb.android:mvrx-testing:0.7.0'
}
```
To use the rule, add the following to a test file:
```kotlin
class MyTest {

  companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule()
   }
}
```
- New: Add `Completable#execute` [#170](https://github.com/airbnb/MvRx/pull/170)
- Improved speed of saving and recreating state with `@PersistState`. [#159](https://github.com/airbnb/MvRx/pull/159)
- Upgrade Kotlin to 1.3.11
- Upgrade to support lib 28.0.0 and fix bug when using 28.0.0 and restoring in a new process. [#150](https://github.com/airbnb/MvRx/pull/150)
- Fix: Allow state arg constructors to accept subtypes [#144](https://github.com/airbnb/MvRx/pull/144).

## Version 0.6.0
*(2018-10-23)*
- New: Option to not re-emit the most recent state value when transitioning from stopped -> start. [#113](https://github.com/airbnb/MvRx/pull/113)
- New: Add ScriptableStateStore interface for UI testing. [#88](https://github.com/airbnb/MvRx/pull/88)
- New: Debug validation for state immutability [#90](https://github.com/airbnb/MvRx/pull/90)
- New: selectSubscribe for 4-7 properties [#125](https://github.com/airbnb/MvRx/pull/125)
- Fix: Execute no longer sets a default subscribeOn thread. [#75](https://github.com/airbnb/MvRx/pull/75)
- Fix: Memory leaks. [#110](https://github.com/airbnb/MvRx/pull/110), [#111](https://github.com/airbnb/MvRx/pull/111)
- Fix: Proper Proguard Rules. [#78](https://github.com/airbnb/MvRx/pull/78)
- Fix: Multiple `setState` called within a `withState` will execute in expected order. [#89](https://github.com/airbnb/MvRx/pull/89)

## Version 0.5.0
*(2018-08-28)*
- Initial Open source release
