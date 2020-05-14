# Change log
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
