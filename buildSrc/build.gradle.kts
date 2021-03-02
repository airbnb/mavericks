repositories {
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    mavenCentral()
    jcenter()
}

plugins {
    java
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.0.0-alpha08")
    implementation("org.jacoco:org.jacoco.core:0.8.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
}
