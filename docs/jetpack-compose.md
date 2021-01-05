# Jetpack Compose

Although Jetpack Compose has not hit 1.0 yet, ensuring that Mavericks will be compatible with it is extremely important. Alongside Compose there is value in using Mavericks as a container for business logic that will pair well with a tree of composable UI nodes.

We haven't settled on a final API yet but have already begin to explore what one might look like. [Here](https://gist.github.com/gpeal/5bed618a843d4d71bb1518fa8317c5df) are some snippets of working code that enables you to use Mavericks with Compose today. It is likely that the final API will look extremely similar to this.

When Compose and the associated tooling (Android Studio, Android Gradle Plugin, etc) are closer to being finalized, we will release an official Compose artifact.
