package com.airbnb.mvrx.sample.anvil.codegen

import com.airbnb.mvrx.sample.anvil.annotation.ContributesViewModel
import com.google.auto.service.AutoService
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.compiler.api.AnvilCompilationException
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.fqName
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

/**
 * This is an anvil plugin that allows ViewModels to use [ContributesViewModel] alone and let this plugin automatically
 * handle the rest of the Dagger wiring required for constructor injection.
 */
@AutoService(CodeGenerator::class)
class ContributesViewModelCodeGenerator : CodeGenerator {

    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(codeGenDir: File, module: ModuleDescriptor, projectFiles: Collection<KtFile>): Collection<GeneratedFile> {
        return projectFiles.classAndInnerClassReferences(module)
            .filter { it.isAnnotatedWith(ContributesViewModel::class.fqName) }
            .flatMap { listOf(generateModule(it, codeGenDir, module), generateAssistedFactory(it, codeGenDir, module)) }
            .toList()
    }

    private fun generateModule(vmClass: ClassReference.Psi, codeGenDir: File, module: ModuleDescriptor): GeneratedFile {
        val generatedPackage = vmClass.packageFqName.toString()
        val moduleClassName = "${vmClass.shortName}_Module"
        val scope = vmClass.annotations.single { it.fqName == ContributesViewModel::class.fqName }.scope()
        val content = FileSpec.buildFile(generatedPackage, moduleClassName) {
            addType(
                TypeSpec.classBuilder(moduleClassName)
                    .addModifiers(KModifier.ABSTRACT)
                    .addAnnotation(Module::class)
                    .addAnnotation(AnnotationSpec.builder(ContributesTo::class).addMember("%T::class", scope.asClassName()).build())
                    .addFunction(
                        FunSpec.builder("bind${vmClass.shortName}Factory")
                            .addModifiers(KModifier.ABSTRACT)
                            .addParameter("factory", ClassName(generatedPackage, "${vmClass.shortName}_AssistedFactory"))
                            .returns(assistedViewModelFactoryFqName.asClassName(module).parameterizedBy(STAR, STAR))
                            .addAnnotation(Binds::class)
                            .addAnnotation(IntoMap::class)
                            .addAnnotation(AnnotationSpec.Companion.builder(viewModelKeyFqName.asClassName(module)).addMember("%T::class", vmClass.asClassName()).build())
                            .build(),
                    )
                    .build(),
            )
        }
        return createGeneratedFile(codeGenDir, generatedPackage, moduleClassName, content)
    }

    private fun generateAssistedFactory(vmClass: ClassReference.Psi, codeGenDir: File, module: ModuleDescriptor): GeneratedFile {
        val generatedPackage = vmClass.packageFqName.toString()
        val assistedFactoryClassName = "${vmClass.shortName}_AssistedFactory"
        val constructor = vmClass.constructors.singleOrNull { it.isAnnotatedWith(AssistedInject::class.fqName) }
        val assistedParameter = constructor?.parameters?.singleOrNull { it.isAnnotatedWith(Assisted::class.fqName) }
        if (constructor == null || assistedParameter == null) {
            throw AnvilCompilationException(
                "${vmClass.fqName} must have an @AssistedInject constructor with @Assisted initialState: S parameter",
                element = vmClass.clazz,
            )
        }
        if (assistedParameter.name != "initialState") {
            throw AnvilCompilationException(
                "${vmClass.fqName} @Assisted parameter must be named initialState",
                element = assistedParameter.parameter,
            )
        }
        val vmClassName = vmClass.asClassName()
        val stateClassName = assistedParameter.type().asTypeName()
        val content = FileSpec.buildFile(generatedPackage, assistedFactoryClassName) {
            addType(
                TypeSpec.interfaceBuilder(assistedFactoryClassName)
                    .addSuperinterface(assistedViewModelFactoryFqName.asClassName(module).parameterizedBy(vmClassName, stateClassName))
                    .addAnnotation(AssistedFactory::class)
                    .addFunction(
                        FunSpec.builder("create")
                            .addModifiers(KModifier.OVERRIDE, KModifier.ABSTRACT)
                            .addParameter("initialState", stateClassName)
                            .returns(vmClassName)
                            .build(),
                    )
                    .build(),
            )
        }
        return createGeneratedFile(codeGenDir, generatedPackage, assistedFactoryClassName, content)
    }

    companion object {
        private val assistedViewModelFactoryFqName = FqName("com.airbnb.mvrx.sample.anvil.di.AssistedViewModelFactory")
        private val viewModelKeyFqName = FqName("com.airbnb.mvrx.sample.anvil.di.ViewModelKey")
    }
}
