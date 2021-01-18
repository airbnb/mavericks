To help you easily access your mocks, include the `mavericks-launcher` artifact and use `MavericksLauncherActivity`. This activity automatically aggregates all mockable Mavericks views in your App and displays them with their available mocks.

![Launcher Home](/images/mock_launcher_home.png) ![Launcher Detail Screen](/images/mock_launcher_detail_page.png) ![Fragment opened from launcher](/images/mock_launcher_opened_fragment.png)

You can select a Fragment and mock and it will be opened in a new Activity.

To access this Launcher Activity, call `MavericksLauncherActivity.show(context)`.

## Customization

By default this loads each Fragment in a plain Activity, but if your Fragments expect a specific host Activity you can customize the Activity they are launched in by setting `MavericksLauncherMockActivity.activityToShowMock` to whichever Activity class you want.

If you set a custom activity this way it should use the `MavericksLauncherMockActivity.showNextMockFromActivity` function to display the mocked view once the Activity is created. For example:

```kotlin
class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
                MavericksLauncherMockActivity.showNextMockFromActivity(
                    activity = this,
                    showView = { mavericksView ->
                       // Use whatever custom code you want to show your Mavericks View
                     }
                )
        }
    }
}
```

## Opening from command line
MavericksLauncherActivity is exported, so you can open it via adb on the command line.

`adb shell am start -n com.your.application.package/com.airbnb.mvrx.launcher.MavericksLauncherActivity`
Just change com.your.application.package to the package of your application.

There is support for two String extras for customization:

`viewNameToOpen` - Open a specific mavericks view or mock by name. This doesn't have to match exactly, the first FQN of a view that contains this string will be opened. If a matching view is not found, the first mock name to match is opened. Matching is case insensitive.

`adb shell am start -n com.airbnb.sample/com.airbnb.mvrx.launcher.MavericksLauncherActivity --es viewNameToOpen MyFragment`

`viewNamePatternToTest` - Matches all views and mocks whose name contains this string. A test flow is started for them to check that they can open without crashing. Matching is case insensitive.

`adb shell am start -n com.airbnb.sample/com.airbnb.mvrx.launcher.MavericksLauncherActivity --es viewNamePatternToTest com_my_package`

### Multiple Words
If you would like to pass multiple words in the pattern, you need to wrap the arguments in quotes. You may need to quote the full command for this to work

`adb shell "am start -n com.airbnb.sample/com.airbnb.mvrx.launcher.MavericksLauncherActivity --es viewNameToOpen 'Default state'"`

You may also find it helpful to define functions in your bash profile to make it easier to call these.

```
# Open the launcher
function mavericks_launcher() {
  adb shell am start -n com.your.application.package/com.airbnb.mvrx.launcher.MvRxLauncherActivity
}

# Test fragments and mocks that match the given pattern.
# ie: mavericks_test my_fragment
function mavericks_test() {
  adb shell am start -n com.airbnb.sample/com.airbnb.mvrx.launcher.MvRxLauncherActivity --es viewNamePatternToTest $1
}

# Test all mocks in the app
function mavericks_test_all() {
  adb shell am start -n com.airbnb.sample/com.airbnb.mvrx.launcher.MvRxLauncherActivity --es viewNamePatternToTest ""
}

# Open the first fragment that matches the pattern
# ie: mavericks_open MyFragment
function mavericks_open() {
  adb shell am start -n com.airbnb.sample/com.airbnb.mvrx.launcher.MvRxLauncherActivity --es viewNameToOpen $1
}
```

### Jetpack Navigation Support

By default the launcher does not work in an app configured for jetpack navigation, since it cannot know how to start a fragment with your navigation graph. You can define a custom activity with your nav graph that the launcher will use instead. See LauncherActivity in the mvrx sample module for an example.

## Automated testing of mocks from the launcher

From the Launcher activity a "Test All" button is available in the Fragment toolbar that, when clicked, will open each mock in series to check for crashes upon initialization.

This offers a sanity check that each mock can be opened and loaded without crashes.

By default this opens each mock in the `MavericksLauncherTestMocksActivity` activity, but that behavior can be changed via setting a custom intent for `MavericksLauncherTestMocksActivity.provideIntentToTestMock`

The testing offered by this is fairly naive and basic, but could be expanded with future work.

## Caveats
Since each mocked fragment is launched in isolation it must be able to function independently, without assuming any other fragments or activities are present. For example, it must not make assumptions about what its parent fragment or parent activity is.

This means that if you need inter-fragment communication you cannot assume another fragment exists, but instead must use decoupled approaches, such as a Dagger injected class.

Note that the same restriction applies for fragments being run in automatic test suites, so all Mavericks views should follow this.

## How It Works
Mavericks inspects the app Dex files to find all mockable Mavericks views and their declared mock states and arguments. This process can take some time, so Fragment and mock information is saved in shared preferences for faster loading on subsequent app starts.

When a mockable view is selected for display the chosen mock state is applied to it (again, using the same technique as in tests). This is done via a mocked State Store which allows us to force a certain state, while blocking external calls to set state. This prevents normal ViewModel or Fragment initialization from overriding the mock state.

If the Fragment uses an existingViewModel then we override that to instead act like activityViewModel. This is necessary since we synthetically jumped into the middle of a flow so previous view models won't exist.

Network (or other Async operations) that are started on fragment initialization are blocked so that they don't override the forced mock state. Once initialization is over the mocked ViewModel state store is switched to a functional store so that the screen can be interacted with like normal, and future state updates due to user interaction are allowed.