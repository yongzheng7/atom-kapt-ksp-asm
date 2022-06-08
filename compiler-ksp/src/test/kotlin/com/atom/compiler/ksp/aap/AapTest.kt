package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.core.KspContext
import com.atom.compiler.ksp.core.KspLog
import com.atom.compiler.ksp.ext.annotationToMap
import com.atom.compiler.ksp.ext.getAnnotationValue
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
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.impl.kotlin.KSTypeImpl
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File

class AapTest {

    val rootPath: String = "D:\\app_git_android\\demo_asm\\test-plugin-compiler\\"

    private val annotationPath = lazy {
        File(
            rootPath,
            "module-annotation\\src\\main\\java\\com\\atom\\module\\annotation\\aap\\AapImpl.kt"
        )
    }

    private val sourcePath =
        lazy { rootPath + "compiler-ksp\\src\\test\\kotlin\\com\\atom\\compiler\\ksp\\aap\\data" }


    /**
     * 获取源码class文件集合
     */
    private fun getSourceFiles(): List<SourceFile> {
        val loadSourceFile =
            SourceFile.loadSourceFile(sourcePath.value)
        val sourceFiles = mutableListOf<SourceFile>()
        sourceFiles.add(SourceFile.fromPath(annotationPath.value))
        sourceFiles.addAll(loadSourceFile)
        return sourceFiles
    }

    @Test
    fun `进行测试注解中获取api的class全限定名`() {
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
                            val apiImpls =
                                resolver.getSymbolsWithAnnotation(AapImpl::class.qualifiedName!!)
                                    .filterIsInstance<KSClassDeclaration>()
                            apiImpls.forEach { clazz ->
                                try {
                                    KspLog.info("SymbolProcessor getValue, ${clazz.getAnnotationValue(AapImpl::class , "api")}")
                                    KspLog.info("SymbolProcessor getMap, ${clazz.annotationToMap(AapImpl::class )}")
                                } catch (e: Exception) {
                                    KspLog.info("2 >> 1 $e")
                                }
                            }
                            return emptyList()
                        }
                    }
                }
            })
            kspArgs.putAll(hashMapOf<String, String>().apply {
                this[AapOptions.DEBUG_OPTION] = "true"
                this[AapOptions.BUNDLE_OPTION] = "appKspModule"
            })
        }.compile()
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `测试进行遍历添加AapImpl的注解的类`() {
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
                            val apiImpls =
                                resolver.getSymbolsWithAnnotation(AapImpl::class.qualifiedName!!)
                                    .filterIsInstance<KSClassDeclaration>()
                            val result = mutableSetOf<AapMeta>()
                            apiImpls.forEach {
                                try {
                                    AapMeta.create(aapContext, it).also { aapMeta ->
                                        KspLog.info("2 >> 0 \n $aapMeta")
                                        result.add(aapMeta)
                                    }
                                } catch (e: Exception) {
                                    KspLog.info("2 >> 1 $e")
                                }
                            }
                            AapMetas(aapContext).addMetasCode(result).assembleCode()
                            return emptyList()
                        }
                    }
                }
            })
            kspArgs.putAll(hashMapOf<String, String>().apply {
                this[AapOptions.DEBUG_OPTION] = "true"
                this[AapOptions.BUNDLE_OPTION] = "appKspModule"
            })
        }.compile()
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }
}