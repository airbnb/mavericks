repositories {
    google()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
    mavenCentral()
    jcenter()
}

plugins {
    java
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:3.6.3")
    implementation("org.jacoco:org.jacoco.core:0.8.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.20-dev-3947")
}
