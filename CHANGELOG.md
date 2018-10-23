# Change log

## Version 0.6.0 (Current development)
- New: Option to not re-emit the most recent state value when transitioning from stopped -> start. #113
- New: Add ScriptableStateStore interface for UI testing. #88
- New: Debug validation for state immutability #90
- Fix: Execute no longer sets a default subscribeOn thread. #75
- Fix: Memory leaks. #110, #111
- Fix: Proper Proguard Rules. #78
- Fix: Multiple `setState` called within a `withState` will execute in expected order. #89

## Version 0.5.0
*(2018-08-28)*
- Initial Open source release