# What's new in Mavericks 2.0

The first thing you'll notice with Mavericks 2.0 is the change from MvRx to Mavericks. When MvRx was first built in 2017, it was nearly impossible to build a complex app without [RxJava](https://github.com/ReactiveX/RxJava). Today, many apps are transitioning to [Kotlin coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) and many new apps will never need RxJava at all. To modernize Mavericks, we rewrote completely the internals with coroutines and removed the RxJava 2 dependency from the core artifact.

All APIs in the core library are now coroutines based.

### API Changes

| MvRx 1.x | Mavericks 2.x  |
| ------ | --------- |
| <pre lang="kotlin">subscribe(): Disposable</pre>    |<pre lang="json">onEach(): Job</pre>|
| <pre lang="kotlin">selectSubscribe(<br>    stateProp: KProperty1<S, A>,<br>    action: (A) -> Unit<br>): Disposable</pre>    |<pre lang="kotlin">onEach(<br>    stateProp: KProperty1<S, A>,<br>    action: suspend (A) -> Unit<br>): Job</pre> |
| <pre lang="kotlin">asyncSubscribe(<br>    stateProp: KProperty1&lt;S, Async&lt;A>>,<br>    onFail: (Throwable) -> Unit,<br>    onSuccess: (A) -> Unit<br>): Disposable</pre>    |<pre lang="kotlin">onAsync(<br>    stateProp: KProperty1&lt;S, Async&lt;A>>,<br>    onFail: suspend (Throwable) -> Unit,<br>    onSuccess: suspend (A) -> Unit<br>): Job</pre> |
| No equivalent    |<pre lang="kotlin">viewModel.stateFlow</pre>|

### Upgrading from 1.x :id=upgrading

Upgrading from MvRx 1.x should be fairly simple. Mavericks 2.0 includes a `mvrx-rxjava2` artifact which adds back all existing RxJava based APIs (although they now wrap the internal coroutines implementaiton).

#### Migrate initialization code
Previously, you needed to pass `debugMode` into your `BaseMvRxViewModel` super class. Now, you initialize MvRx one time in Application.onCreate. To do so, call `Mavericks.initialize(this)`. Because `debugMode` is no longer passed into each ViewModel individually, you don't even need a base ViewModel class anymore. You can simply extend `BaseMvRxViewModel` each time or migrate to just using `MavericksViewModel` if you no longer need the rxjava2 APIs.
