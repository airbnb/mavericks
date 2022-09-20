package com.gpeal.droidconanvilsample.anvilcompilers


import com.google.auto.service.AutoService
import com.gpeal.droidconanvilsample.lib.daggerscopes.AppScope
import com.gpeal.droidconanvilsample.lib.daggerscopes.ContributesApi
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.fqName
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import dagger.Module
import dagger.Provides
import dagger.Reusable
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import retrofit2.Retrofit
import java.io.File

@Suppress("unused")
@AutoService(CodeGenerator::class)
class ContributesApiCodeGenerator : CodeGenerator {

    override fun isApplicable(context: AnvilContext) = true

    override fun generateCode(codeGenDir: File, module: ModuleDescriptor, projectFiles: Collection<KtFile>): Collection<GeneratedFile> {
        return projectFiles.classAndInnerClassReferences(module)
            .filter { it.isAnnotatedWith(ContributesApi::class.fqName) }
            .map { generateModule(it, codeGenDir) }
            .toList()
    }

    private fun generateModule(apiClass: ClassReference, codeGenDir: File): GeneratedFile {
        val generatedPackage = apiClass.packageFqName.toString()
        val moduleClassName = "${apiClass.shortName}_Module"
        val scope = AppScope::class.asClassName()
        // Generate a Dagger module file called MyApi_Module.
        val content = FileSpec.buildFile(generatedPackage, moduleClassName) {
            addType(
                TypeSpec.objectBuilder(moduleClassName)
                    .addAnnotation(Module::class)
                    .addAnnotation(AnnotationSpec.builder(ContributesTo::class).addMember("%T::class", scope).build())
                    .addFunction(
                        // @Provides @Reusable provideMyApi(retrofit: Retrofit): MyApi
                        FunSpec.builder("provide${apiClass.shortName}")
                            .addParameter("retrofit", Retrofit::class.asClassName())
                            .returns(apiClass.asClassName())
                            .addAnnotation(Provides::class)
                            .addAnnotation(Reusable::class)
                            // return retrofit.create(MyApi::class.java)
                            .addCode("return retrofit.create(%T::class.java)", apiClass.asClassName())
                            .build(),
                    )
                    .build(),
            )
        }
        return createGeneratedFile(codeGenDir, generatedPackage, moduleClassName, content)
    }
}
