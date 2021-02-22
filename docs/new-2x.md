# What's new in Mavericks 2.0

### MvRx -> Mavericks

The first thing you'll notice with Mavericks 2.0 is the change from MvRx to Mavericks. When MvRx was first built in 2017, it was nearly impossible to build a complex app without [RxJava](https://github.com/ReactiveX/RxJava). Today, many apps are transitioning to [Kotlin coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) and many new apps will never need RxJava at all. To modernize Mavericks, we completely rewrote the internals with coroutines and removed the RxJava 2 dependency from the core artifact.

All APIs in the core library are now coroutines based.

### API Changes

APIs that take suspending lambdas will auto-cancel the previous instance if it hasn't finished by the time the next value emits. In other words, they behave like [Flow<T>.mapLatest](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/map-latest.html).

| MvRx 1.x | Mavericks 2.x  |
| ------ | --------- |
| <pre lang="kotlin">subscribe(...): Disposable</pre>    |<pre lang="json">onEach(...): Job</pre>|
| <pre lang="kotlin">selectSubscribe(<br>    stateProp: KProperty1<S, T>,<br>    action: (T) -> Unit,<br>): Disposable</pre>Plus multi-prop overloads    |<pre lang="kotlin">onEach(<br>    stateProp: KProperty1<S, T>,<br>    action: suspend (T) -> Unit,<br>): Job</pre>Plus multi-prop overloads |
| <pre lang="kotlin">asyncSubscribe(<br>    stateProp: KProperty1&lt;S, Async&lt;T>>,<br>    onFail: (Throwable) -> Unit,<br>    onSuccess: (T) -> Unit,<br>): Disposable</pre>    |<pre lang="kotlin">onAsync(<br>    stateProp: KProperty1&lt;S, Async&lt;T>>,<br>    onFail: suspend (Throwable) -> Unit,<br>    onSuccess: suspend (T) -> Unit,<br>): Job</pre> |
| `execute` is extension on Observable&lt;T>, Single&lt;T>, and Completable| `execute` is extension on Flow&lt;T>, suspend () -> T, and Deferred&lt;T>|
| No equivalent    |<pre lang="kotlin">Flow&lt;T>.setOnEach(dispatcher: Dispatcher, reducer: suspend (T) -> Unit): Job</pre> |
| No equivalent    |<pre lang="kotlin">ViewModel&lt;S>.stateFlow: Flow&lt;S></pre>|
| No equivalent    |<pre lang="kotlin">suspend ViewModel&lt;S>.awaitState(): S</pre>|

### New Functionality

#### Retain Value for execute

Mavericks can now retain previously successful values across subsequent `Loading` and `Fail` states. You can read more about it [here](/async?id=retaining-data-across-reloads-with-retainvalue).


#### `Flow<T>.setOnEach { copy(...) }`

Sometimes, you don't want to map a stream of data to `Async<T>`, you just want to map a stream of data to a state property. To do that, you can call `Flow<T>.setOnEach { copy(...) }`. When you do that, Mavericks will subscribe to the flow, call your reducer each time with each item, then cancel the job when the ViewModel is cleared.

#### MavericksViewModel no longer extends Jetpack ViewModel

MavericksViewModel can be used and will behave exactly like it used to. However, the underlying implementation no longer extends Jetpack's own ViewModel. Instead, it is just a normal Kotlin class which gets wrapped in a Jetpack ViewModel under the hood. This means that you can create your own instances of MavericksViewModels and use them outside of the standard delegates.   

### Upgrading from 1.x :id=upgrading

Upgrading from MvRx 1.x should be fairly simple. Mavericks 2.0 includes a `mavericks-rxjava2` artifact which adds back all existing RxJava based APIs (although they now wrap the internal coroutines implementation).

#### Migrate initialization code

Previously, you needed to pass `debugMode` into your `BaseMvRxViewModel` super class. Now, you initialize MvRx one time in Application.onCreate. To do so, call `Mavericks.initialize(this)`. Because `debugMode` is no longer passed into each ViewModel individually, you don't even need a base ViewModel class anymore. You can simply extend `BaseMvRxViewModel` each time or migrate to just using `MavericksViewModel` if you no longer need the rxjava2 APIs.

#### Update BaseMvRxFragment

With MvRx 1.x, you had to make your base Fragment class extend `BaseMvRxFragment`. Now, you can make it just implement `MavericksView`. The rxjava2 artifact still ships with `BaseMvRxFragment` but it is deprecated and everything will continue to work with the `MavericksView` interface. 

#### Update tests

In MvRx 1.x, [MvRxTestRule](https://github.com/airbnb/MvRx/blob/1.5.1/testing/src/main/kotlin/com/airbnb/mvrx/test/MvRxTestRule.kt#L31) would set global RxJava schedulers to be synchronous (trampoline) schedulers for tests. This was required for MvRx state stores to behave synchronously. However, your tests may have implicitly relied on this behavior. As a result, you may need to create your own RxRule to add back this behavior. One such example of an RxRule can be found [here](https://gist.github.com/gpeal/8b3af17f75d1450b431842bfe05b4d5c).
