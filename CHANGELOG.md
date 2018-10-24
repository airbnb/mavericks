# Change log

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
