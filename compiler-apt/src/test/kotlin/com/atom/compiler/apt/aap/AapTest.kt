package com.atom.compiler.apt.aap

import com.atom.compiler.apt.aap.data.Teacher
import com.atom.compiler.apt.core.AptContext
import com.atom.compiler.apt.core.AptLog
import com.atom.compiler.apt.ext.annotationToMap
import com.atom.compiler.apt.ext.getMyValue
import com.atom.compiler.test.core.*
import com.atom.compiler.test.core.SourceFile.Companion.loadSourceFile
import com.atom.module.annotation.aap.AapImpl
import org.assertj.core.api.Assertions
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*

class AapTest {

    lateinit var rootPath: String

    val aapImplPath: File
        get() {
            return File(
                rootPath,
                "module-annotation\\src\\main\\java\\com\\atom\\module\\annotation\\aap\\AapImpl.kt"
            )
        }

    val savePath: String
        get() {
            return rootPath + "compiler-apt\\src\\test\\kotlin\\com\\atom\\compiler\\apt\\aap\\result"
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
                setOf(AapImpl::class.java.canonicalName).also {
                    println("getSupportedAnnotationTypes 1 ${AapImpl::class.java.canonicalName}")
                }

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
            sources = getSourceFiles()
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
    fun `测试使用 AapKspImpl 注解 , 看api为Kclass能否获取到对应的全类型明`() {
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
                        println("test_aap2 1  $it")
                        val typeMirror = it.getMyValue(AapImpl::class.java, "api")
                        println("test_aap2 2  $typeMirror")
                        val annotationToMap = it.annotationToMap(AapImpl::class.java)
                        println("test_aap2 3  $annotationToMap")
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
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val result = defaultCompilerConfig().apply {
            sources = getSourceFiles()
            annotationProcessors = listOf(AapProcessor())
            inheritClassPath = true
            kaptArgs.putAll(hashMapOf<OptionName, OptionValue>().apply {
                this.put("debug", "true")
                this.put("bundleClassname", "app")
            })
            compilerPlugins = listOf(mockPlugin)
        }.compile()
        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }


    @Test
    fun `测试注解处理器,进行收集实现某些接口的类`() {
        val annotationProcessor = object : AbstractProcessor() {
            lateinit var aapContext: AapContext
            override fun init(processingEnv: ProcessingEnvironment?) {
                super.init(processingEnv)
                processingEnv?.also {
                    AptContext.init(it)
                    AptLog.init(it.messager, true)
                    aapContext = AapContext(AptContext, it.options)
                }
            }

            override fun getSupportedAnnotationTypes(): Set<String> =
                setOf(AapImpl::class.java.canonicalName).also {
                    println("getSupportedAnnotationTypes 1 ${AapImpl::class.java.canonicalName}")
                }

            override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
                println("annotationProcessor process")
                //遍历所有的class类,筛选出指定索引标注的类
                val apiImpls: HashSet<AapMeta> = HashSet()
                p1?.getElementsAnnotatedWith(AapImpl::class.java)?.forEach {
                    try {
                        apiImpls.add(AapMeta.create(aapContext, it))
                    } catch (e: Exception) {
                        AptLog.error("process find exception=$e ")
                    }
                }
                AptLog.info("process assemble finish \n $apiImpls")
                AptLog.info("process assemble final class start  \n $aapContext")
                AapMetas(aapContext).addMetasCode(apiImpls).assembleCode()
                AptLog.info("process assemble final class end  \n $aapContext")
                return false
            }
        }
        val mockPlugin = Mockito.mock(ComponentRegistrar::class.java)
        val result = defaultCompilerConfig().apply {
            sources = getSourceFiles()
            annotationProcessors = listOf(annotationProcessor)
            compilerPlugins = listOf(mockPlugin)
            inheritClassPath = true
            kaptArgs.putAll(hashMapOf<OptionName, OptionValue>().apply {
                this.put(AapOptions.DEBUG_OPTION, "true")
                this.put(AapOptions.BUNDLE_OPTION, "app")
            })
        }.compile()
        println(result)
    }
}