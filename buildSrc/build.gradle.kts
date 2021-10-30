repositories {
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    mavenCentral()
}

plugins {
    java
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.0.0")
    implementation("org.jacoco:org.jacoco.core:0.8.7")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
}
