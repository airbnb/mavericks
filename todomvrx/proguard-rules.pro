# This file contains project specific proguard rules

-android

# Print out the full proguard config used for every build
-printconfiguration build/outputs/fullProguardConfig.pro

#
# Keep rules that are used because MvRx uses Kotlin, Kotlin reflection and RxJava. These are not defined in the
# lib as they are not specific to the lib itself but need to be most likely present in any project that uses
# Kotlin, Kotlin reflection and RxJava.
#

# These classes are used via kotlin reflection and the keep might not be required anymore once Proguard supports
# Kotlin reflection directly.
-keep interface kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoader
-keep class kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoaderImpl
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

#
# Keep rules that are unique to the sample project because it uses epoxy
#

# The code is safeguarded against API28 use so we can just ignore the warning bout Handler.createAsync(looper) not existing.
# Can be removed when compiled against API >=28
-dontwarn com.airbnb.epoxy.EpoxyAsyncUtil
