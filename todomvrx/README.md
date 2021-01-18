# ToDo Mavericks

![gif](todo.gif)

This ToDo app follows the [specification](https://github.com/googlesamples/android-architecture/wiki/To-do-app-specification) outlined in the [Google Samples Android Architecture](https://github.com/googlesamples/android-architecture) repo.


It is 100% feature complete and has significantly fewer lines of code compared to other architecture blueprints.
Running `cloc` on `src/main` for Mavericks yields:

| Language LOC     | **Mavericks**   | **mvp-kotlin** | **mvvm-live-kotlin** |  **mvp** | **mvp-clean** | **mvp-java** |
| -------------    |     -----  |     ---------  |     ---------------- |  ------- | ------------- | ------------ |
| **Kotlin**       |         803|           1541 |                 1564 |        0 |             0 |            0 |
| **Java**         |         0  |              0 |                    0 |     2171 |          2777 |         2145 |
| **XML**          |         501|            608 |                  751 |      608 |           608 |          611 |
| **Total**        |   **1304** |       **2149** |             **2315** | **2779** |      **2385** |     **2756** |

## Architecture
ToDo-Mavericks uses Room, Epoxy, and Navigation Components for its core architecture.

It then uses [TasksViewModel](https://github.com/airbnb/mavericks/blob/master/todomvrx/src/main/java/com/airbnb/mvrx/todomvrx/TasksViewModel.kt) to act as an in-memory cache and owner of multiple repositories. It could merge db + network but for the sake of having an actual single source of truth, it loads two different database sources that connect to the same database but have different artifical delays. It then proxies all tasks calls to each of them.

All task state is shared between Fragments using this ViewModel.

Snackbars for actions like adding a task are handled using [this block of code](https://github.com/airbnb/mavericks/blob/master/todomvrx/src/main/java/com/airbnb/mvrx/todomvrx/core/BaseFragment.kt#L46) which compares state changes to see when tasks have changed.
