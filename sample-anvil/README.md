# Anvil Sample

The Anvil sample consists of 3 modules:
* :sample-anvil: The actual application that demonstrates an example features.
* :sample-anvilcodegen: The [anvil-compiler code generator](https://github.com/square/anvil/blob/main/compiler-api/README.md) that allows `@ContributesViewModel` to setup all of the wiring necessary for constructor dependency injection.
* :sample-anvilannotations: Shared annotations between the sample and codegen.

The Anvil sample demonstrates how to setup a `@ContributesViewModel` annotation that allows you to do constructor injection from Dagger without any additional wiring such as the creation of a Dagger module. The setup requires a bit of infrastructure which is all included in these three modules. However, once it is set up, it dramatically simplifies the usage of ViewModels and Dagger.

A good starting point is ExampleFeatureFragment. ExampleFeatureFragment and the other classes in that folder demonstrate:
* How to use `@ContributesViewModel` to do constructor injection.
* How to create feature/Fragment scoped Dagger Components that are supported by `@ContributesViewModel`.
* How to set up App/User/Feature Dagger component hierarchies that are commonly used in real world apps.
