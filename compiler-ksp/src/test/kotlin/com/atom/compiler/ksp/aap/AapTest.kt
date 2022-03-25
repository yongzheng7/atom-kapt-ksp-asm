package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.AapSymbolProcessorProvider
import com.atom.compiler.test.core.KotlinCompilation
import com.atom.compiler.test.core.SourceFile
import com.atom.compiler.test.core.defaultCompilerConfig
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

    @Before
    fun before() {
        rootPath = "D:\\app_git_android\\demo_asm\\test-plugin-compiler"
    }

    @Test
    fun test() {
        println("test 1")
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        println(mockPlugin)
        println(mockPlugin.javaClass.name)
        println("test 2")
    }


    @Test
    fun `test simple add annotations`() {
        val annotationProcessor = object : AbstractProcessor() {
            override fun getSupportedAnnotationTypes(): Set<String> =
                setOf(AapImpl::class.java.canonicalName)

            override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
                p1?.getElementsAnnotatedWith(AapImpl::class.java)?.forEach {
                    println("annotationProcessor ${it?.simpleName.toString()}")
                }
                return false
            }
        }
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val classAapImpl =
            SourceFile.fromPath(
                File(
                    rootPath,
                    "\\aap-annotation\\src\\main\\java\\com\\atom\\module\\aap\\annotation\\AapImpl.kt"
                )
            )
        val loadSourceFile =
            loadSourceFile(
                File(
                    rootPath,
                    "\\compiler\\compiler-ksp\\src\\test\\kotlin\\com\\atom\\compiler\\ksp\\aap\\data"
                ).absolutePath
            )
        val sourceFiles = mutableListOf<SourceFile>()
        sourceFiles.add(classAapImpl)
        sourceFiles.addAll(loadSourceFile)

        val result = defaultCompilerConfig().apply {
            sources = sourceFiles
            annotationProcessors = listOf(annotationProcessor)
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()
        println(result)
    }

    fun loadSourceFile(path: String): List<SourceFile> {
        val root = File(path)
        if (!root.exists()) return emptyList()
        val listFiles = root.listFiles() ?: return emptyList()
        return listFiles.map {
            SourceFile.fromPath(it)
        }
    }

    @Test
    fun test_aap() {
        // 一定需要注解
        val classAapImpl =
            SourceFile.fromPath(
                File(
                    rootPath,
                    "\\module-annotation\\src\\main\\java\\com\\atom\\module\\annotation\\aap\\AapImpl.kt"
                )
            )
        val loadSourceFile =
            loadSourceFile(
                File(
                    rootPath,
                    "\\compiler-ksp\\src\\test\\kotlin\\com\\atom\\compiler\\ksp\\aap\\data"
                ).absolutePath
            )
        val sourceFiles = mutableListOf<SourceFile>()
        sourceFiles.add(classAapImpl)
        sourceFiles.addAll(loadSourceFile)
        val result = KotlinCompilation().apply {
            sources = sourceFiles
            symbolProcessorProviders = listOf(AapSymbolProcessorProvider())
        }.compile()

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun test_aap2() {
        val classAapImpl =
            SourceFile.fromPath(File("D:\\project\\testkaptksp\\aap-annotation\\src\\main\\java\\com\\atom\\module\\aap\\annotation\\AapImpl.kt"))
        val loadSourceFile =
            loadSourceFile("D:\\project\\testkaptksp\\test-kapt-ksp\\src\\test\\kotlin\\com\\atom\\compiler\\aap\\data")
        val sourceFiles = mutableListOf<SourceFile>()
        sourceFiles.add(classAapImpl)
        sourceFiles.addAll(loadSourceFile)

        val result = defaultCompilerConfig().apply {
            sources = sourceFiles
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

}