# Fragment Arguments

Although Mavericks, doesn't have to be used with Fragments, it has some helpers in case it is.

### Setting Mavericks Fragment arguments
When creating a Fragment:
1. Create a `Parcelable` or `Serializable` object
2. Set the Fragment arguments to `yourArgs.asMavericksArgs()`

```kotlin
val fragment = YourFragment()
fragment.arguments = yourArgs.asMavericksArgs()
```

### Retrieving your arguments in your Fragment
Create a property in your fragment:
```kotlin
private val args: YourArgsType by args()
```
That's it!

### Using Fragment args in the initial value for `MavericksState`
Create a secondary constructor that has 1 parameter of the same type as your Fragment arguments. Mavericks will automatically get the arguments off your Fragment and call this constructor with them when creating your State.
```kotlin
data class MyState(
  val itemId: String,
  val item: Async<Item> = Uninitialized
) : MavericksState {
  constructor(args: YourArgs) : this(args.itemId)
}
```
