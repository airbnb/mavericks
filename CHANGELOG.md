# Change log

## Version 0.7.0
*(2019-1-11)*
- **BREAKING:** `MvRxViewModelFactory` has been updated to provide more flexiblity when creating ViewModel and initial state [#169](https://github.com/airbnb/MvRx/pull/169): 
  - Type signature changed from `MvRxFactory<S>` to `MvRxFactory<S, VM>` for better type safety.
  - Changed signature of `MvRxViewModelFactory#create(activity: FragmentActivity, state: S) : VM` to `MvRxViewModelFactory#create(viewModelContext: ViewModelContext, state: S) : VM?`. `ViewModelContext` contains a reference to the owning activity, and MvRx fragment arguments. For fragment scoped view models, it will be of type `FragmentViewModelContext` with a reference to the creating fragment. This changes allow Depedency injection on the your ViewModel.
  - New: Added `MvRxViewModelFactory#initialState(viewModelContext: ViewModelContext) : S?`. This is useful if your initial state requires information from a component other than `args`.
  - New: `@JvmStatic` no longer required on `MvRxViewModelFactory#create` and new `MvRxViewModelFactory#initialState`


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
