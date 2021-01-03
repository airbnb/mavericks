# Mavericks + RxJava2

MvRx 1.x was designed to work well with RxJava 2. However, Mavericks now uses coroutines internally.

RxJava 2 compatibility is still available via the `mvrx-rxjava2` artifact.

To use it, use `BaseMvRxViewModel` instead of `MavericksViewModel`. Doing so will give you access to `execute` extensions on `Single<T>` and `Observable<T>` as well as all other MvRx 1.x APIs.
