# Proguard/Dexguard/R8

The Mavericks lib itself has a consumer Proguard file that configures Proguard. As the main lib uses Kotlin, and optional add-on modules use Kotlin reflection and RxJava, you might have to add rules to handle those to the Proguard rules of your app itself. (If you did not add them for other purposes already). These are not part of the consumer Proguard file, as they are not part of the lib specific Proguard configuration. This is what that part of the configuration looks like for our sample app:

```
# These classes are used via kotlin reflection and the keep might not be required anymore once Proguard supports
# Kotlin reflection directly.
-keep class kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoaderImpl
-keep class kotlin.reflect.jvm.internal.impl.serialization.deserialization.builtins.BuiltInsLoaderImpl
-keep class kotlin.reflect.jvm.internal.impl.load.java.FieldOverridabilityCondition
-keep class kotlin.reflect.jvm.internal.impl.load.java.ErasedOverridabilityCondition
-keep class kotlin.reflect.jvm.internal.impl.load.java.JavaIncompatibilityRulesOverridabilityCondition

# If Companion objects are instantiated via Kotlin reflection and they extend/implement a class that Proguard
# would have removed or inlined we run into trouble as the inheritance is still in the Metadata annotation
# read by Kotlin reflection.
# FIXME Remove if Kotlin reflection is supported by Pro/Dexguard
-if class **$Companion extends **
-keep class <2>
-if class **$Companion implements **
-keep class <2>

# https://medium.com/@AthorNZ/kotlin-metadata-jackson-and-proguard-f64f51e5ed32
-keep class kotlin.Metadata { *; }

# https://stackoverflow.com/questions/33547643/how-to-use-kotlin-with-proguard
-dontwarn kotlin.**

# RxJava
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
```

#### Kotlin reflection with Proguard/Dexguard/R8

The configuration provided for the lib assumes that your shrinker can correctly handle Kotlin `@Metadata` annotations (e.g. Dexguard 8.6++) if it cannot (e.g. Proguard and current versions of R8) you need to add these rules to the proguard configuration of your app:
```
# MavericksViewModel loads the Companion class via reflection and thus we need to make sure we keep
# the name of the Companion object.
-keepclassmembers class ** extends com.airbnb.mvrx.MavericksViewModel {
    ** Companion;
}

# Members of the Kotlin data classes used as the state in Mavericks are read via Kotlin reflection which cause trouble
# with Proguard if they are not kept.
# During reflection cache warming also the types are accessed via reflection. Need to keep them too.
-keepclassmembers,includedescriptorclasses,allowobfuscation class ** implements com.airbnb.mvrx.MavericksState {
   *;
}

# The MavericksState object and the names classes that implement the MavericksState interface need to be
# kept as they are accessed via reflection.
-keepnames class com.airbnb.mvrx.MavericksState
-keepnames class * implements com.airbnb.mvrx.MavericksState

# MavericksViewModelFactory is referenced via reflection using the Companion class name.
-keepnames class * implements com.airbnb.mvrx.MavericksViewModelFactory
```
