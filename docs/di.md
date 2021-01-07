# Dependency Injection

Mavericks provides the configuration required to integrate into whatever dependency injection system you use.

Dagger is the most commonly used dependency injection framework and used by Airbnb and Tonal. For that reason, Dagger is the recommended dependency injection framework for new apps using Mavericks.  

## Dagger

`MavericksViewModelFactory` can be combined with [AssistedInject](https://github.com/square/AssistedInject) to provide seamless integration with Dagger.
For more information and working example, checkout our [hellodagger](https://github.com/airbnb/MvRx/tree/master/hellodagger) example on GitHub.

Dagger is [in the process](https://github.com/google/dagger/pull/2215) of providing `@AssistedInject` capabilities in the main library so this will be offered without any additional dependencies soon.

## Koin

Similar architectures have been built for Koin [here](https://github.com/airbnb/MvRx/pull/432).
