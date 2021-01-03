## Use derived props when possible :id=derived

[Derived properties](core-concepts.md#derived-properties) are extremely useful at scale because they are so easy to reason about and test. Whenever something _can_ be modeled as a derived property, you probably _should_ put it there.
Examples:
```kotlin
data class CounterState(
    val count: Int = 0
) : MavericksState {
    val isEven = count % 2 == 0
}
```
```kotlin
data class SignUpState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
) : MavericksState {
    val hasName = firstName.isNotBlank() && lastName.isNotBlank()

    val hasValidEmail = EMAIL_PATTERN.matches(email)

    val canSubmitForm = hasName && hasValidEmail
}
```

## Working with immutable maps
Use [custom copy and delete extension functions](https://gist.github.com/gpeal/3cf0907fc80094a833f9baa309a1f627) and treat them similar to data classes:

`setState { copy(yourMap = yourMap.copy(“a” to 1, “b” to 2)) }`

or

`setState { copy(yourMap = yourMap.delete(“a”, “b”)) }`

## Drive animations from ViewMode.onEach

You may use an `onEach` callback to drive an animation or show an error for a specific amount of time like this:
```kotlin
viewModel.onAsync(YourState::yourProp, onFail = {
    binding.error.isVisible = true
    delay(4000)
    binding.error.isVisible = false
})
```

If your Fragment View gets destroyed, Mavericks will stop emitting new values but it will _not_ cancel any existing ones. In this case, if the Fragment view gets destroyed during the delay, your app will crash when you try and access it to hide the error after 4 seconds. To get around this, replace that code with:
```kotlin
viewModel.onAsync(YourState::yourProp, onFail = {
    viewLifecycleOwner.lifecycleScope.whenStarted {
        binding.error.isVisible = true
        delay(4000)
        binding.error.isVisible = false
    }
})
```

`whenStarted` is part of Jetpack's [lifecycle aware coroutine support](https://developer.android.com/topic/libraries/architecture/coroutines#suspend).