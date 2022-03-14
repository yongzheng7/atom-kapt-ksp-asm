package com.atom.compile.core.test

import com.atom.compile.core.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

class CompilerPluginsTest {

    @Test
    fun ComponentRegistrar_Test() {
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        println(mockPlugin)
        println(mockPlugin.javaClass.name)
    }

    @Test
    fun componentregistrar_test_create_kotlin_class() {
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val result = defaultCompilerConfig().apply {
            sources = listOf(SourceFile.new("emptyKotlinFile.kt", ""))
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()
        println(result)
    }

    @Test
    fun test_add_default_compiler() {
        val annotationProcessor = object : AbstractProcessor() {
            override fun getSupportedAnnotationTypes(): Set<String> =
                setOf(ProcessElem::class.java.canonicalName)

            override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
                p1?.getElementsAnnotatedWith(ProcessElem::class.java)?.forEach {
                    println("annotationProcessor ${it?.simpleName.toString()}")
                    Assert.assertEquals("JSource", it?.simpleName.toString())
                }
                return false
            }
        }
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val jSource = SourceFile.kotlin(
            "JSource.kt", """
				package com.atom.compile.core

				@ProcessElem
				class JSource {
					fun foo() { }
				}
					"""
        )

        val result = defaultCompilerConfig().apply {
            sources = listOf(jSource)
            annotationProcessors = listOf(annotationProcessor)
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()
        println(result)
    }

    @Test
    fun `when compiler plugins are added they get executed`() {

        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)

        val result = defaultCompilerConfig().apply {
            sources = listOf(SourceFile.new("emptyKotlinFile.kt", ""))
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()

        verify(mockPlugin, atLeastOnce()).registerProjectComponents(any(), any())

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `when compiler plugins and annotation processors are added they get executed`() {
        val annotationProcessor = object : AbstractProcessor() {
            override fun getSupportedAnnotationTypes(): Set<String> {
                return setOf(ProcessElem::class.java.canonicalName)
            }

            override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
                p1?.getElementsAnnotatedWith(ProcessElem::class.java)?.forEach {
                    println("annotationProcessor ${it?.simpleName.toString()}")
                    Assert.assertEquals("JSource", it?.simpleName.toString())
                }
                return false
            }
        }

        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)

        val jSource = SourceFile.kotlin(
            "JSource.kt", """
				package com.atom.compile.core

				@ProcessElem
				class JSource {
					fun foo() { }
				}
					"""
        )
        val annotationSource = SourceFile.kotlin(
            "ProcessElem.kt", """
				package com.atom.compile.core

                import kotlin.reflect.KClass

				@MustBeDocumented
                @kotlin.annotation.Target(AnnotationTarget.CLASS)
                @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
                annotation class ProcessElem()
					"""
        )

        val result = defaultCompilerConfig().apply {
            sources = listOf(annotationSource, jSource)
            annotationProcessors = listOf(annotationProcessor)
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()

        verify(mockPlugin, atLeastOnce()).registerProjectComponents(any(), any())

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `when JS compiler plugins are added they get executed`() {
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)

        val result = defaultJsCompilerConfig().apply {
            sources = listOf(SourceFile.new("emptyKotlinFile.kt", ""))
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()

        verify(mockPlugin, atLeastOnce()).registerProjectComponents(any(), any())
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }
}
