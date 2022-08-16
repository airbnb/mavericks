## Unit Testing
When writing unit tests for view models it is helpful to force the backing coroutines to run synchronously, as well as to have control over the state of the viewmodel.

Mavericks provides support for managing this in both JUnit 4 and 5 tests via the optional `mavericks-testing` artifact.

To use, apply either the test rule or test extension, configured to your needs via the appropriate properties. See the kdoc on the respective classes for the most up to date documentation on usage.

By default, the rule/extension will:
- Disable lifecycle awareness of viewmodel observers
- Make state store operations synchronous
- Disable debug checks on viewmodels
- Swap TestCoroutineDispatcher for the Main coroutine dispatcher.

### JUnit 5 Extension

Add the `MvRxTestExtension` to your test's companion object.
```kotlin
    @RegisterExtension
    val mavericksTestExtension = MavericksTestExtension()
```

### JUnit 4 Rule

Add the `MvRxTestRule` rule to your test.

```kotlin
    @get:Rule
    val mavericksRule = MavericksTestRule()
```
