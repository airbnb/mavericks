# ToDo MvRx

![gif](todo.gif)

This ToDo app follows the [specification](https://github.com/googlesamples/android-architecture/wiki/To-do-app-specification) outlined in the [Google Samples Android Architecture](https://github.com/googlesamples/android-architecture) repo.


It is 100% feature complete and has significantly fewer lines of code compared to other archtiecture blueprints.
Running `cloc` on `src/main` for MvRx yields:
## MvRx
| Language LOC     | **MvRx**   | **mvp-kotlin** | **mvvm-live-kotlin** | **mvp** | **mvp-clean** | **mvp-java** |
| -------------    |     -----  |     ---------  |     ---------------- | ------- | ------------- | ------------ |
| **Kotlin**       |         803|           1541 |                 1564 |       0 |             0 |            0 |
| **Java**         |         0  |              0 |                    0 |    2171 |          2777 |         2145 |
| **XML**          |         501|            608 |                  751 |     608 |           608 |          611 |
| **Total**        |        1304|           2149 |                 2315 |    2779 |          2385 |         2756 |


## From Android Architecture Blueprints
### todo-mvp-rjava
| Language      | Lines of code |
| ------------- | ------------- |
| **Java**    |             2145|
| **XML**       |            611|
| **Total**     |           2756|