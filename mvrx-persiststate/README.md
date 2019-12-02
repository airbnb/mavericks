# mvrx-persiststate

This extension library for MvRx provides an `@PersistState` annotation. `@PersistState` allows you to annotate parcelable or serializable state properties to persist them [across processes](https://medium.com/inloopx/android-process-kill-and-the-big-implications-for-your-app-1ecbed4921cb). Refer to the docs for `@PersistState` for more information.

Versioning is kept in sync with mvrx so you should use a shared variable in your build.gradle file.

This library is a separate addon because it requires kotlin-reflect which is 2mb.