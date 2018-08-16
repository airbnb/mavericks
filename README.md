# MvRx: Android Development turbocharged

## For full documentation, check out the [wiki](https://github.com/airbnb/MvRx/wiki)

MvRx is the Android framework from Airbnb that we use for nearly all product development on Android.

When we began creating MvRx, our goal was not to create our own MVP/MVVM/MVC/MVI pattern for Airbnb, it was to make building products easier, faster, and more fun. All of our decisions have built on that. We believe that for MvRx to be successful, it must be effective for building everything from the simplest of screens to the most complex in our app.

MvRx is Kotlin first and Kotlin only. By being Kotlin only, we could leverage several powerful language features for a cleaner API. If you are not familiar with Kotlin, in particular, [data classes](https://kotlinlang.org/docs/reference/data-classes.html), and [receiver types](https://kotlinlang.org/docs/reference/lambdas.html#function-literals-with-receiver), please run through [Kotlin Koans](https://kotlinlang.org/docs/tutorials/koans.html) or other Kotlin tutorials before continuing with MvRx.

MvRx is built on top of the following existing technologies and concepts:
* Kotlin
* Android Architecture Components
* RxJava
* React (conceptually)
* [Epoxy](https://github.com/airbnb/epoxy) (optional but recommended)
