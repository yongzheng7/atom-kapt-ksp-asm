package com.atom.compiler.apt.aap

import com.atom.compiler.apt.aap.data.Teacher
import com.atom.compiler.test.core.*
import com.atom.compiler.test.core.SourceFile.Companion.loadSourceFile
import com.atom.compiler.test.ksp.symbolProcessorProviders
import com.atom.module.annotation.aap.AapImpl
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

class AapTest {

    lateinit var rootPath: String

    val aapImplPath: File
        get() {
            return File(
                rootPath,
                "module-annotation\\src\\main\\java\\com\\atom\\module\\annotation\\aap\\AapImpl.kt"
            )
        }

    @Before
    fun before() {
        rootPath = "D:\\app_git_android\\demo_asm\\test-plugin-compiler\\"
    }

    fun getSourceFiles(): List<SourceFile> {
        val loadSourceFile =
            loadSourceFile("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\compiler-apt\\src\\test\\kotlin\\com\\atom\\compiler\\apt\\aap\\data")
        val sourceFiles = mutableListOf<SourceFile>()
        sourceFiles.add(SourceFile.fromPath(aapImplPath))
        sourceFiles.addAll(loadSourceFile)
        return sourceFiles
    }

    @Test
    fun test() {
        println("test 1")
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        println(mockPlugin)
        println(mockPlugin.javaClass.name)
        println("test 2")

        val classFile = SourceFile.getClassFile(Teacher::class.java)
        println(classFile)
    }


    @Test
    fun `test simple add annotations`() {
        val annotationProcessor = object : AbstractProcessor() {
            override fun getSupportedAnnotationTypes(): Set<String> =
                setOf(AapImpl::class.java.canonicalName)

            override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
                println("annotationProcessor process")
                p1?.getElementsAnnotatedWith(AapImpl::class.java)?.forEach {
                    println("annotationProcessor ${it?.simpleName.toString()}")
                }
                return false
            }
        }
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val result = defaultCompilerConfig().apply {
            sources = listOf(
                SourceFile.fromPath(aapImplPath),
                SourceFile.fromPath(File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\compiler-apt\\src\\test\\kotlin\\com\\atom\\compiler\\apt\\aap\\data\\Teacher.kt"))
            )
            annotationProcessors = listOf(annotationProcessor)
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()
        println(result)
    }


    @Test
    fun test_aap2() {
        val result = defaultCompilerConfig().apply {
            sources = getSourceFiles()
            annotationProcessors = listOf(object : AbstractProcessor() {
                override fun getSupportedAnnotationTypes(): MutableSet<String> {
                    return mutableSetOf<String>().apply {
                        this.add(AapImpl::class.java.canonicalName)
                    }
                }

                override fun process(
                    annotations: MutableSet<out TypeElement>?,
                    roundEnv: RoundEnvironment?
                ): Boolean {
                    if (roundEnv?.processingOver() == true || annotations?.size == 0) {
                        return false
                    }
                    roundEnv?.getElementsAnnotatedWith(
                        AapImpl::class.java
                    )?.onEach {
                        println("test_aap2  $it")
                    }
                    return false
                }
            })
            inheritClassPath = true
        }.compile()
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }
    @Test
    fun `test AapProcessor`() {
        val result = defaultCompilerConfig().apply {
            sources = getSourceFiles()
            annotationProcessors = listOf(AapProcessor())
            inheritClassPath = true
            kaptArgs.putAll(hashMapOf<OptionName, OptionValue>().apply {
                this.put("debug" , "debug kaptArgs")
            })
        }.compile()
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }
}