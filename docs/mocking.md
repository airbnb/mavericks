# Mocking
Mavericks includes tooling to mock ViewModel state, which can then be used for automated and manual testing.

This mocking support is added with the optional `mavericks-mocking` artifact, and includes tooling to:

- Force (ie "mock") a specific MavericksState for a MavericksViewModel, while preventing (and listening for) subsequent changes
- Force all ViewModels within a MavericksView to specific states
- Generate Kotlin code that reconstructs State snapshots off of a running device, which can then be used for mocking
- Easily modify mocked states to define state variations
- Display all MaverickViews and their mocked states in a Launcher activity, with the ability to open up to any mocked state

## Philosophy

With Mavericks, the State classes of each ViewModel ideally completely represent and define the content and behavior of a screen. By loading specific mocks we can test how the screen behaves with that state.

This can be used to run automated tests on a screen, setting various mock states and asserting correct behavior, which can include screenshot tests or tests that verify behavior upon interaction.

Additionally, this can be helpful when developing or manually testing the app, as any screen in the app can be opened to any specific state, potentially avoiding many clicks to reach an otherwise deeply nested screen.

Defining mock states in Kotlin source code can help to stub out network or other async requests, and also ensures that mocks are valid at compile time (as opposed to a mocking system that might use JSON or otherwise specify mocks outside of source code)

For more technical details, read the article series on how Airbnb developed this testing infrastructure - [https://medium.com/airbnb-engineering/better-android-testing-at-airbnb-3f5b90b9c40a](https://medium.com/airbnb-engineering/better-android-testing-at-airbnb-3f5b90b9c40a)

## Initialization and Configuration

1. Add an additional dependency on the `mavericks-mocking` artifact in your build.gradle file. Versioning is the same as the core Mavericks library.

2. Initialize Mavericks via a call to `MockableMavericks.initialize` when your application is created. (This replaces the normal call to `Mavericks.initialize`)

3. Use the `MockableMavericksView` interface in place of the normal `MavericksView` and override the `provideMocks` function to define mock states (guidance below). If you are using `mavericks-rxjava2` you can have your classes implement both `MvRxView` and `MockableMavericksView`.

4. To support generating mocks for an active screen, call the `registerMockPrinter` when the view is initialized (for example, in `onCreate` of the Fragment)

## Providing Mock States

For each Mavericks screen that implements `MockableMavericksView` (eg a Fragment) you can override the `provideMocks` function to define mocks for the ViewModels in that view.

The Mavericks mocking artifact provides several extension functions to help you define these mock states. Depending on how many ViewModels the view uses, you should use one of the following functions:

- `mockNoViewModels` - If the view has no viewmodels (It may still have Fragment arguments)
- `mockSingleViewModel` - When the view has one view model
- `mockTwoViewModels` - When the view has two view models
- Additional functions follow this naming pattern for higher view model counts

Each of these functions follows a similar pattern for the parameters it requires:

1. For each view model, the function requires a reference to the view model property in the view, as well as a "default" state for that view model (Explained below)
2. If your view requires arguments to be initialized (such as Fragment arguments), default arguments must be provided
3. A mock builder lambda that allows you to specify additional mock variations besides the default (Details described below)

For example, a Fragment that has a single view model and no arguments might have this implementation:
```kotlin
class MyFragment : Fragment, MockableMavericksView {
    
    override fun provideMocks() = mockSingleViewModel(
        viewModelReference = MyFragment::myViewModelProperty,
        defaultState = myViewModelState,
        defaultArgs = null
    ) {
        // Optionally declare additions state variations here
    }
}
```

If your Fragment uses the Mavericks paradigm of passing a Parcelable data class in the Arguments under the key `Mavericks.KEY_ARG` then you can use an instance of that class as your default arguments.
```kotlin
class MyFragment : Fragment, MockableMavericksView {
    
    override fun provideMocks() = mockSingleViewModel(
        viewModelReference = MyFragment::myViewModelProperty,
        defaultState = myMockedViewModelState,
        defaultArgs = MyCustomArgs(id = 1)
    ) {
        // Optionally declare additions state variations here
    }
}

@Parcelize
data class MyCustomArgs(
 val id: Long
): Parcelable
```

If you don't use the `Mavericks.KEY_ARG` for your arguments you can pass a Bundle to `defaultArgs` and it will be passed along as-is when the Fragment is mocked.

### The Default State and Arguments

The `defaultState` and `defaultArgs` parameters passed to the top level mock function should be thought of as the canonical representation of data on that page. Generally, no properties in it should be null, have an empty List or Collection, be undefined, be in a loading or error state, etc.

A default mock (named “Default state”) is created for you automatically based on the default state you pass in. Additionally, if default args are provided, a “Default initialization” mock is also automatically created. This tests initializing your ViewModel with the default arguments and the state that results from those arguments.

Mock variations to the default state can describe possible differences a user might encounter, such as data in a loading or error state.

The purpose of this is two-fold:

1. It standardizes a canonical representation of the screen which you can then use as a basis for testing
2. It allows variations to be easily defined and tested in manageable pieces

Additionally, Mavericks automatically adds another default mock named "Default state after process recreation". This is based on default state, but has the Mavericks state saving and restoring operations applied to it to simulate how the state reacts to process death.

## How Mocks Are Used

Once a view has defined mocks for its ViewModels via `provideMocks`, Mavericks processes them at runtime and packages them into a convenient class for use.

This class provides a function that returns a new instance of the view that is instantiated with the arguments defined in the mock (if any), and with all ViewModels frozen to the state defined in the mock.

A Launcher activity is provided by default that allows you to browse and open your mocked Views, but you can also access the low level mocking mechanisms if you would like to use them for custom testing.

### Launching Mocked Screens

Mavericks provides a built in entry point for accessing mocks via the [**mock launcher**](launcher.md).

### Custom Usage Of Mocks
For a given mockable view, a `MockedViewProvider` is created for each mock. This contains a lambda (`createView`) you can invoke to create an instance of that view. It will be initialized with the arguments specified in the mock, and automatically have its ViewModels forced to the states defined in that mock.

To access these mock view providers, use the functions inside the `ViewMocker.kt` file - either `getMockVariants` or `mockVariants`.

Accessing mocks manually this way can be helpful if you are setting up automated testing for your screens.

#### Advanced Configuration and Usage

The `MockableMavericks` object that is used for initializing Mavericks has default configurations that works for basic usage. However, if you are using mocks to create custom testing systems for your screens you can leverage advanced configuration to have more control and visibility into the mocks.

Mocking behavior is controlled via two main global properties that control ViewModel creation:

 - `Mavericks.viewModelDelegateFactory`
 - `Mavericks.viewModelConfigFactory`

The `MockableMavericks` object uses the implementations in `MockViewModelDelegateFactory` and `MockMavericksViewModelConfigFactory` to set values for these properties. You have the option to toggle settings on these implementations, subclass them to modify their behavior, or build your own implementations entirely.

The `MockMavericksViewModelConfigFactory` in particular is helpful for modifying mock behavior for custom tests. The implementation can be accessed by casting the property `Mavericks.viewModelConfigFactory` and its behavior can then be configured.

In `MockMavericksViewModelConfigFactory` you can change the `mockBehavior` property, which specifies the default behavior of mocked view models. At it's core, "mocking" means forcing a specific state on a ViewModel. However, there are several nuances to behavior that can be important to control based on your purposes. These include:

- Whether new state can be set over the initial mocked state
- How to handle ViewModel's in a view that are injected with "existingViewModel"
- What to do when a ViewModel attempts to load data asynchronously with `execute`
- If new state changes are allowed, do they happen async as in production, or synchronously to facility deterministic testing
- Do mocked views get subscribed update their update upon state changes

`mockBehavior` allows you to control these aspects by specifying default behavior for each mocked screen. In addition, you can change the behavior of an existing mocked screen by using `MockMavericksViewModelConfigFactory.pushMockBehaviorOverride`. For example, this can be helpful in cases where you want to start the screen as fully mocked with state changes blocked, and then later on allow state change to test clicks.

The `MavericksViewModelConfigFactory` also provides a function `addOnConfigProvidedListener` that you can use to listen for the instantiation of each ViewModel. This is helpful to get a hook into the creation of each ViewModel.

When a view is mocked, Mavericks internally tracks the View instance as well as its ViewModels, so that it can properly mock them. To prevent these references from leaking after you are done using the mocked view you can invoke `cleanupMockState` on the `MockedView` provided by the `MockedViewProvider`.

You can alternatively access and clear the entire global store of all mocks via `MockableMavericks.mockStateHolder`.

## Generating Mocks

Mavericks ViewModel state is implemented with Kotlin data classes, so a mocked implementation of a State is defined by Kotlin code that instantiates a State class and provides test values for each data class property.

For this to be effective it is important that the mocked data is as extensive as possible. For complex state classes with many fields it can be tedious to manually write the Kotlin code required to create these mocks. Mocks can potentially be thousands of lines of code.

To help you create mocks, Mavericks provides a Mock Printer tool that you can run from your local computer when you have a device connected via ADB that is running your app. When this tool is run it will send an intent to your app that will tell Mavericks to generate the Kotlin code needed to recreate the States for any ViewModels that are currently on screen. This generated code will then be pulled from the device by the script and written to local `.kt` files on your machine so that you can use them as mock implementations.

Essentially, this allows you to capture a snapshot of the State of any of your Mavericks Views, save it to a source file, and reload it as a mocked view at any time in the future for testing.

For this to work you must first make sure `MockableMavericksView.registerMockPrinter` is called when your view is created. This registers a lifecycle observer on your MavericksView that will use a Broadcast Receiver to listen for the scripts intent while the view is in the "Started" lifecycle state.

The script itself is written in Kotlin and packaged as a standalone executable that you can download from the Mavericks Github repository. It can be found at `mock_generation/MavericksMockPrinter`

While your app is live and attached via ADB (with debugging enabled), run the mock printer tool via `./mock_generation/MavericksMockPrinter` from your computer. It is recommended to run this from your app's root project directory so the generated mock source files can be copied to the right directory for you.

You can run the tool with the help flag - `./MavericksMockPrinter -h`

It is recommended that you generate and save a single fully mocked state per ViewModel, which will be the "default state" that you pass to the `provideMocks` function of your mockable View.

You can then create variations of this mocked state via the DSL described below. This approach reduces how many complete mocks you have to maintain.

### Advanced Usage Notes and Configuration

The mock printer uses reflection to identify the primary constructor properties of your State class, inspect the values at runtime, and create Kotlin code that can instantiate another instance of the class with the same values. This is done recursively to capture the state of all nested data structures.

This is possible because Kotlin data classes have a predictable syntax for their construction via named parameter arguments to their primary constructor.

However, this also means that if there is any class contained within your State (including at any nested level) that is not a Kotlin data class or primitive (eg a Java class), then the mock printer will not be able to generate code to accurately reconstruct it.

You can instruct the Mock Printer how to handle types like these by implementing the `TypePrinter` interface and adding your implementations to `MockableMavericks.mockPrinterConfiguration.customTypePrinters`. You will have to create your own instance of `MockPrinterConfiguration`.

For example, some apps may have legacy `AutoValue` java classes. Mavericks provided a `AutoValueTypePrinter` that recognizes AutoValue generated classes and knows how to properly generate code to capture their state.

You can also use the `MockPrinterConfiguration` to control which package name the generated mocks will have.

## Defining Mock State Variations

Once default state has been set up, you can declare mock variations to your arguments or state. Each variation should be thought of as a test - and like most tests, it should target one specific behavior in your View.

Ideally mock variations would test all realistic data permutations that a user might encounter. Often though this is not realistic or helpful to define variations for all possible data permutations - instead, try to target common cases or expected edge cases such as error states, loading, or nullable properties.

Mock variations are defined via a Kotlin DSL with the `state` function. Each variation has a "name" parameter that describes it.

```kotlin
val defaultState = MyState(...)

override fun provideMocks() = mockSingleViewModel(MyFragment::MyViewModel, defaultState) {

    // Each mock is defined with the "state" function.
    // The name should describe the variation, and
    // the state it represents should be returned from the lambda
    state(name = "Null user") {
        MyState(user = null)
    }
}
```

Generally, since State objects are complex we don't want to create a new one from scratch for each variation. Instead, we use Kotlin's data class copy function to modify the "default state" with the change we want.

The default state is the receiver of the state lambda, so we can call copy directly in the lambda

```kotlin
val defaultState = MyState(...)

override fun provideMocks() = mockSingleViewModel(MyFragment::MyViewModel, defaultState) {

    state(name = "Null user") {
        // The receiver, or "this", is the defaultState from mockSingleViewModel
        copy(user = null)
    }
}
```

Modifying the default state like this makes it much easier to define a variation. This is why earlier sections emphasize the importance of generating a comprehensive default state. Your collection of mocks for testing can consist of the canonical default state along with the many slight variations that you may want to test.

Complex state objects often have deeply nested data, which can be tedious to change using the copy function.

```kotlin
val state = MyState(
    account = Account(
        user = User(
            name = "Brian"
        )
    )
)

// Set user name to null... gross :(
state.copy(account = state.account.copy(user = state.account.user.copy(name = null)))
```

As a simpler alternative you can use the `set` function, which is a DSL tool that exists only within this mocking context

```kotlin
val defaultState = MyState(
    account = Account(
        user = User(
            name = "Brian"
        )
    )
)

override fun provideMocks() = mockSingleViewModel(MyFragment::MyViewModel, defaultState) {

    state(name = "Null user name") {
        // This DSL says that we want to set the nested property 'name'
        // to be null
        set { ::account { ::user { ::name } } }.with { null }
    }
}
```

This DSL for setting a property works by specifying one nested property along with the value it should be set to. The properties use the property reference syntax to specify which property in the object should be modified. Each lambda block represents another nesting layer in the object hierarchy.

Note that this ONLY works for Kotlin data classes. Also, since our data is immutable it doesn't modify the original state, but copies it with the specified property updated - the new object is returned.

If you need to change multiple properties you can chain set calls:

```kotlin
state("null user name and null email") {
   set { ::account { ::user { ::name } } }.with { null }
   .set { ::account { ::user { ::email } } }.with { null }
}
```

Remember, this doesn't mutate the original state, so only the single state object that is returned is used for the new state variation. This means that if you have multiple `set` calls they must be chained with a `.`

### Mocking Multiple ViewModels

Defining mock state variations for multiple view models is very similar to the case with a single view model. The only difference is that for each state we need to define a specific view model.

For this you can use the functions `mockTwoViewModels` and `mockThreeViewModels` (similar variations exist for even more view models if needed).

```kotlin
provideMocks() = mockTwoViewModels(
    viewModel1Reference = SearchFragment::searchResultsViewModel,
    defaultState1 = mockSearchState,
    viewModel2Reference = SearchFragment::userAccountViewModel,
    defaultState2 = userAccountState,
    defaultArgs = SearchArgs(query = "Hawaii")
) {
    state(name = "no query, no user") {
        viewModel1 {
          setNull { ::query }
        }

        viewModel2 {
            setNull { ::user }
        }
    }
}
```

When using a single view model in earlier examples the `state` function's lambda just had to return a single state object for the single view model.

Now with two view models the lambda uses a builder object as its receiver, which we can use to specify which view model to set a new state for this variation, via the functions `viewModel1` and `viewModel2`.

By default each view model inherits its default state, so we can choose to change the state of only one of the view model's. In this case view model 2 keeps its default state for this varation, while view model 1 makes a change.

```kotlin
    state(name = "no query") {
        viewModel1 {
          setNull { ::query }
        }
    }
```

### Helper Functions

There are a few variations on the `set` DSL to help with common cases.

* `setNull` to set any property value to null
* `setTrue` or `setFalse` to change a Boolean value
* `setEmpty` to set a List property to an empty list
* `setZero` to set a number property to zero

For example `set { ::account { ::user { ::name } } }.with { null }` could be shortened to `setNull { ::account { ::user { ::name } } }`

**Setting a property inside an Async property:**
Add the `success` block to represent an Async property in the Success state.
```kotlin
setTrue { ::searchResults { success { ::resultCount } } }
```

**Define a mock variation for the loading and failure states of an Async property:**
This is useful for creating two mocks, for loading and failure, in a single short line
```kotlin
stateForLoadingAndFailure { ::searchResults }
```

Note that this only works for Async properties at the top level of the State object.

If you are mocking two view models you can instead use `viewModel1StateForLoadingAndFailure` and `viewModel2StateForLoadingAndFailure`

Alternatively you can individually modify loading or error state:

```kotlin
state("Loading") {
    setLoading { ::searchResults }
}

state("Failed") {
    setNetworkFailure { ::searchResults }
}
```

### Mocking Arguments

If your fragment takes arguments, then your mock function must define default arguments:
```kotlin
mockSingleViewModel(MyFragment::MyViewModel, defaultState, defaultArgs)
```

These arguments are provided to every mock variation, so that when the mocked fragment is created it is initialized with the arguments, and then has the mocked state overlaid via the viewmodel.

Mavericks viewmodels automatically create initial state from fragment arguments, and this is tested for you as well. A dedicated initialization mock is automatically created using the `defaultArgs` you provide.

If you would like to test other argument initializations for your fragment you can do that with the `args` function.

```kotlin
mockSingleViewModel(MyFragment::MyViewModel, defaultState, defaultArgs) {

    args("null id") {
        setNull { ::id }
    }
}
```

This operates very similarly to mocks declared with the state function. The default arguments are the receiver to the lambda and you must return a new instance of your arguments.
This example assumes that the arguments are a data class that uses the Mavericks.KEY_ARG pattern for passing arguments to a view in a bundle.

If your fragment accesses arguments directly (instead of just using them to initialize it's MavericksState) - then you may want to test interactions between specific arguments and state. You can do that by passing arguments to a state mock function.

```kotlin
mockSingleViewModel(MyFragment::MyViewModel, defaultState, defaultArgs) {

    state(
       name = "null user name and args missing id",
       args = { setNull { ::id } }
    ) {
        setNull { ::listing { ::user { ::name } } }
    }
}

If args are not provided to a state variation then the default args are used.
```

### Combining Mocks

If your View has many mocks, or there are different default States or Arguments that your mocks are tested with, you can split your mocks into groups using the `combineMocks` function.

```kotlin
override fun provideMocks() = combineMocks(
    "Standard Mocks" to standardMocks(),
    "Other Mocks" to otherMocks()
)
```

This allows us to separate the implementations into separate mock files, which makes defining many mocks simpler and cleaner. This can also be helpful if your mocks are very large, or have large variations that can't be easily captured via the normal mock variation DSL.


