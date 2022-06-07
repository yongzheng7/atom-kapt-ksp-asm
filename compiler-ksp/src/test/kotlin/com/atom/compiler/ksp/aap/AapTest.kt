package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.common.KspContext
import com.atom.compiler.ksp.common.KspLog
import com.atom.compiler.test.core.*
import com.atom.compiler.test.ksp.kspArgs
import com.atom.compiler.test.ksp.symbolProcessorProviders
import com.atom.module.annotation.aap.AapImpl
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File

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
    fun `测试进行遍历添加AapKspImpl的注解的类 2`() {
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val result = KotlinCompilation().apply {
            compilerPlugins = listOf(mockPlugin)
            sources = getSourceFiles()
            symbolProcessorProviders = listOf(object : SymbolProcessorProvider {
                override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
                    return object : SymbolProcessor {
                        lateinit var aapContext: AapContext
                        override fun process(resolver: Resolver): List<KSAnnotated> {
                            KspContext.init(environment, resolver)
                            KspLog.init(environment.logger, true)
                            KspLog.info("SymbolProcessor Version, ${KotlinVersion.CURRENT}")
                            KspLog.info("SymbolProcessor Options, ${environment.options}")
                            aapContext = AapContext(KspContext, environment.options)
                            val apiImpls = resolver.getSymbolsWithAnnotation(AapImpl::class.qualifiedName!!)
                                    .filterIsInstance<KSClassDeclaration>()
                                    .map { AapMeta(aapContext, it) }
                                     .toSet()
                            try {
                                apiImpls.iterator().forEach {
                                    KspLog.info("2 >> 0 \n $it")
                                }
                            }catch (e : Exception){
                                KspLog.info("2 >> 1 $e")
                            }
                            AapMetas(aapContext).writeTo(apiImpls)
                            return emptyList()
                        }
                    }
                }
            })
            kspArgs.putAll(hashMapOf<String, String>().apply {
                this.put(AapOptions.DEBUG_OPTION, "true")
                this.put(AapOptions.BUNDLE_OPTION, "app")
            })
        }.compile()
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }
}