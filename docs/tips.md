## Use derived props when possible

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
