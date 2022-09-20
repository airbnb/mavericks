package com.gpeal.droidconanvilsample.anvilcompilers

import com.google.common.truth.Truth.assertThat
import com.squareup.anvil.compiler.internal.testing.compileAnvil
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import dagger.Provides
import dagger.Reusable
import org.junit.Test

class ContributesApiCodeGeneratorTest {

    @Test fun `a dagger module is generated`() {
        compileAnvil(
            """
                package com.test
                
                import com.gpeal.droidconanvilsample.lib.daggerscopes.ContributesApi

                @ContributesApi
                interface TestApi
            """
        ) {
            assertThat(exitCode).isEqualTo(OK)

            val clazz = classLoader.loadClass("com.test.TestApi_Module")
            val providerMethod = clazz.declaredMethods.single()

            assertThat(providerMethod.returnType).isEqualTo(classLoader.loadClass("com.test.TestApi"))
            assertThat(providerMethod.annotations.map { it.annotationClass }).containsExactly(Provides::class, Reusable::class)
        }
    }
}