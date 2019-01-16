# Change log

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
