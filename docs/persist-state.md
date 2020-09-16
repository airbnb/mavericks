# Persistence
In Android, there are two major cases in which persistence must be considered:

#### Configuration Changes / Back Stack
Traditionally, you would use savedInstanceState to save important information. However, with Mavericks, you will get the exact same instance of your ViewModel back so you have everything you need to re-render the view. Generally, screens won't need to implement savedInstanceState with Mavericks.

#### Restoration in a New Process
Sometimes, Android will kill your process to save memory and restore your app in a new process with just savedInstanceState. In this case, Google recommends saving just enough data such as ids so that you can re-fetch what you need. When you return to your app, Mavericks will attempt to recreate all ViewModels that existed in the original process. In most cases, the initial state created from default state or fragment arguments (see above) is enough. However, if you would like to persist individual pieces in state, you may annotate properties in your state class with `@PersistState`. Note that these properties must be Parcelable or Serializable and may not be Async objects because persisting and restoring loading states would lead to a broken user experience.

```kotlin
data class FormState(
  @PersistState val firstName: String = "",
  @PersistState val lastName: String = "",
  @PersistState val homeTown: String = ""
) : MavericksState
```

In this case, if the process is destroyed and recreated, the first name, last name, and home town will already be populated by Mavericks in your ViewModel's initial state.
