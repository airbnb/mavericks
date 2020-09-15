object Versions {
    // Build tools and SDK
    const val buildTools = "29.0.3"
    const val compileSdk = 29
    const val gradlePlugin = "4.0.0"
    const val kotlin = "1.4.10"
    const val minSdk = 16
    const val targetSdk = 29

    // Android libraries
    const val arch = "2.1.0"
    const val appcompat = "1.2.0"
    const val cardview = "1.0.0"
    const val constraintlayout = "2.0.0"
    const val core = "1.3.1"
    const val fragment = "1.2.5"
    const val lifecycle = "2.2.0"
    const val navigation = "2.2.1"
    const val recyclerview = "1.1.0"
    const val coordinatorLayout = "1.1.0"
    const val room = "2.2.5"

    // Libraries
    const val autoValue = "1.6.6"
    const val kotlinCoroutines = "1.3.9"
    const val dagger = "2.27"
    const val daggerAssisted = "0.5.2"
    const val epoxy = "4.0.0"
    const val koin = "2.0.1"
    const val lottie = "3.4.0"
    const val moshi = "1.9.2"
    const val multidex = "2.0.1"
    const val mvrx = "1.3.0"
    const val picasso = "2.8"
    const val retrofit = "2.7.2"
    const val rxAndroid = "2.1.1"
    const val rxJava = "2.2.9"

    // Instrumented testing libraries
    const val espresso = "3.2.0"

    // Testing libraries
    const val junit = "4.13"
    const val junitExt = "1.1.1"
    const val mockito = "2.25.1"
    const val mockitoKotlin = "2.2.0"
    const val mockk = "1.9.3"
    const val robolectric = "4.3.1"
    const val testCore = "1.2.0"
}

object AnnotationProcessors {
    const val autoValue = "com.google.auto.value:auto-value:${Versions.autoValue}"
    const val dagger = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val daggerAssisted = "com.squareup.inject:assisted-inject-processor-dagger2:${Versions.daggerAssisted}"
    const val epoxy = "com.airbnb.android:epoxy-processor:${Versions.epoxy}"
    const val lifecycle = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"
    const val moshi = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val room = "androidx.room:room-compiler:${Versions.room}"
}

object Libraries {
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val autoValue = "com.google.auto.value:auto-value-annotations:${Versions.autoValue}"
    const val cardview = "androidx.cardview:cardview:${Versions.cardview}"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.core}"
    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerAssisted = "com.squareup.inject:assisted-inject-annotations-dagger2:${Versions.daggerAssisted}"
    const val epoxy = "com.airbnb.android:epoxy:${Versions.epoxy}"
    const val espressoIdlingResource = "androidx.test.espresso:espresso-idling-resource:${Versions.espresso}"
    const val fragment = "androidx.fragment:fragment:${Versions.fragment}"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    const val fragmentTesting = "androidx.fragment:fragment-testing:${Versions.fragment}"
    const val junit = "junit:junit:${Versions.junit}"
    const val koin = "org.koin:koin-android:${Versions.koin}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val lifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
    const val lottie = "com.airbnb.android:lottie:${Versions.lottie}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val multidex = "androidx.multidex:multidex:${Versions.multidex}"
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    const val picasso = "com.squareup.picasso:picasso:${Versions.picasso}"
    const val recyclerview = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
    const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:${Versions.coordinatorLayout}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitMoshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    const val retrofitRxJava = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val roomRxJava = "androidx.room:room-rxjava2:${Versions.room}"
    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJava}"
    const val viewModelSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycle}"
    const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
}

object InstrumentedTestLibraries {
    const val core = "androidx.test:core:${Versions.testCore}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val junit = "androidx.test.ext:junit:${Versions.junitExt}"
}

object TestLibraries {
    const val junit = "junit:junit:${Versions.junit}"
    const val kotlinCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val roboeletric = "org.robolectric:robolectric:${Versions.robolectric}"
}
