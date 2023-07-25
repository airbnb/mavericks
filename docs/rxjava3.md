# Mavericks + RxJava3

[//]: # (TODO refine the documentation)

MvRx 1.x was designed to work well with RxJava 2. 

However, Mavericks now uses coroutines internally.

RxJava 3 compatibility is available via the `mavericks-rxjava3` artifact.

To use it, use `BaseMvRxViewModel` instead of `MavericksViewModel`. Doing so will give you access to `execute` extensions on `Single<T>` and `Observable<T>` as well as all other MvRx 1.x APIs.
