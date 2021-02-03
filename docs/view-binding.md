# View Binding

[View Binding](https://developer.android.com/topic/libraries/view-binding) is Google's replacement for `findViewById()`, Kotlin synthetic accessors, and [Butterknife](https://github.com/JakeWharton/butterknife).
Although not strictly Mavericks related, our sample apps use custom delegates to easily use View Binding with a single line of code.
Add [FragmentViewBindingDelegate.kt](https://github.com/airbnb/mavericks/blob/release/2.0.0/sample/src/main/java/com/airbnb/mvrx/sample/utils/FragmentViewBindingDelegate.kt) to your project then you can use it like this:
```kotlin
class CounterFragment : BaseFragment(R.layout.counter_fragment) {
  private val binding: CounterFragmentBinding by viewBinding()
  private val viewModel: CounterViewModel by fragmentViewModel()

  override fun invalidate() = withState(viewModel) { state ->
    binding.textView.text = "Count: ${state.count}"
  }
}
```

The `by viewBinding()` delegate feels very similar to the Mavericks View Model delegate syntax so the two play well together.
