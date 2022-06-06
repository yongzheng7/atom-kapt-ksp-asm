package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.common.KspContext
import com.atom.compiler.ksp.common.KspLog
import com.atom.compiler.test.core.KotlinCompilation
import com.atom.compiler.test.core.SourceFile
import com.atom.compiler.test.core.defaultCompilerConfig
import com.atom.compiler.test.ksp.symbolProcessorProviders
import com.atom.module.annotation.aap.AapKspImpl
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.impl.kotlin.KSTypeImpl
import com.squareup.kotlinpoet.ksp.toClassName
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

    val annotationPath: File
        get() {
            return File(
                rootPath,
                "module-annotation\\src\\main\\java\\com\\atom\\module\\annotation\\aap\\AapKspImpl.kt"
            )
        }

    val savePath: String
        get() {
            return rootPath + "compiler-ksp\\src\\test\\kotlin\\com\\atom\\compiler\\apt\\aap\\result"
        }

    val sourcePath: String
        get() {
            return rootPath + "compiler-ksp\\src\\test\\kotlin\\com\\atom\\compiler\\ksp\\aap\\data"
        }

    @Before
    fun before() {
        rootPath = "D:\\app_git_android\\demo_asm\\test-plugin-compiler\\"
    }

    fun getSourceFiles(): List<SourceFile> {
        val loadSourceFile =
            SourceFile.loadSourceFile(sourcePath)
        val sourceFiles = mutableListOf<SourceFile>()
        sourceFiles.add(SourceFile.fromPath(annotationPath))
        sourceFiles.addAll(loadSourceFile)
        return sourceFiles
    }

    @Test
    fun ` 检查进行测试验证`() {
        println("test 1")
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        println(mockPlugin)
        println(mockPlugin.javaClass.name)
        println("test 2")
    }


    @Test
    fun `测试进行遍历添加AapKspImpl的注解的类`() {
        val annotationProcessor = object : AbstractProcessor() {
            override fun getSupportedAnnotationTypes(): Set<String> =
                setOf(AapKspImpl::class.java.canonicalName)

            override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
                p1?.getElementsAnnotatedWith(AapKspImpl::class.java)?.forEach {
                    println("annotationProcessor ${it.javaClass} ${it is KSClassDeclaration}  ${it.modifiers} ${it?.simpleName.toString()}")
                }
                return false
            }
        }
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val result = defaultCompilerConfig().apply {
            sources = getSourceFiles()
            annotationProcessors = listOf(annotationProcessor)
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
        }.compile()
        println(result)
    }

    @Test
    fun test_aap() {
        val result = KotlinCompilation().apply {
            sources = getSourceFiles()
            symbolProcessorProviders = listOf(object : SymbolProcessorProvider {
                override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
                    return object : SymbolProcessor {
                        override fun process(resolver: Resolver): List<KSAnnotated> {
                            KspContext.init(environment, resolver)
                            KspLog.init(environment.logger, true)
                            KspLog.info("DeepCopySymbolProcessor, ${KotlinVersion.CURRENT}")
                            resolver.getSymbolsWithAnnotation(AapKspImpl::class.qualifiedName!!)
                                .filterIsInstance<KSClassDeclaration>()
                                .forEach {
                                    KspLog.info("1 ${it.qualifiedName?.getShortName()} ")
                                    it.annotations.forEach {
                                        KspLog.info("2 ${it.arguments} ")
                                        it.arguments.forEach {
                                            if(it.value is KSTypeImpl){
                                                KspLog.info("3 ${it.name?.getShortName()}, ${it.value.toString()} ," +
                                                        " ${(it.value as KSTypeImpl).toClassName()} ")
                                            }else{
                                                KspLog.info("3 ${it.name?.getShortName()}, ${it.value.toString()}")
                                            }
                                        }
                                    }
                                }
                            return emptyList()
                        }
                    }
                }
            })
        }.compile()
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }
}