package com.airbnb.mvrx

import org.gradle.testing.jacoco.tasks.JacocoReport

@Suppress("LeakingThis") // Gradle requires Tasks be open classes
open class JacocoReportTask : JacocoReport() {
    init {
        group = "Reporting"
        description = "Generate Jacoco coverage reports for Debug build"

        @Suppress("UnstableApiUsage")
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }

        val excludes = listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*"
        )
        val buildDir = project.buildDir

        val debugTree = project.fileTree(buildDir.toString() + "/intermediates/classes/debug") {
            exclude(excludes)
        }
        val kotlinDebugTree = project.fileTree(buildDir.toString() + "/tmp/kotlin-classes/debug") {
            exclude(excludes)
        }

        classDirectories.from(arrayOf(debugTree), arrayOf(kotlinDebugTree))
        sourceDirectories.from(
            arrayOf(
                project.android.sourceSets.getByName("main").java.srcDirs,
                "${project.projectDir}/src/main/kotlin"
            )
        )
        executionData.from("$buildDir/jacoco/testDebugUnitTest.exec")
    }
}
