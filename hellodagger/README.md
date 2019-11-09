# Dagger Usage Sample for MvRx

This module contains a sample app to demonstrate how to ease the usage of Dagger and AssistedInject in apps using MvRx.

## Key Features

* **Injecting state into ViewModels with AssistedInject**
  
  Since the `initialState` parameter is only available at runtime, Dagger can not provide this dependency for us. We need the [AssistedInject](https://github.com/square/AssistedInject) library for this purpose.

* **Multibinding setup for AssistedInject Factories**

  Every ViewModel using AssistedInject needs a Factory interface annotated with `@AssistedInject.Factory`. These factories are grouped together under a common parent type `AssistedViewModelFactory` to enable a Multibinding Dagger setup.

* **Removing boilerplate from a MvRxViewModelFactory**

  An `MvRxViewModelFactory` is different than an AssistedInject Factory, and is still needed. Using this AssistedInject multibinding setup, most ViewModels will share the same boilerplate logic in their `MvRxViewModelFactory`'s. A `DaggerMvRxViewModelFactory` has been added to eliminate this boilerplate.
