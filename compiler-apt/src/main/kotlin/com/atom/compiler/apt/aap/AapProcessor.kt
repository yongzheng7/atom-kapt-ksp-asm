package com.atom.compiler.apt.aap

import com.atom.compiler.apt.core.AptProcessor
import com.atom.compiler.apt.core.AptContext
import com.atom.compiler.apt.core.AptLog
import com.atom.module.annotation.aap.AapImpl
import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class AapProcessor : AptProcessor() {
    lateinit var aapContext : AapContext
    override fun initOptions(context: AptContext, options: Map<String, String>) {
        AptLog.info("AapProcessor initOptions $options")
        aapContext = AapContext(context, options)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        AptLog.info("process start******************************************")
        if (annotations.isNullOrEmpty()) {
            return false
        }
        if (roundEnv == null || roundEnv.processingOver()) {
            return false
        }
        //遍历所有的class类,筛选出指定索引标注的类
        val apiImpls: HashSet<AapMeta> = HashSet()
        for (element in roundEnv.getElementsAnnotatedWith(AapImpl::class.java)) {
            try {
                apiImpls.add(AapMeta.create(aapContext, element).also {
                    AptLog.info("process AapMeta.create=$it")
                })
            }catch (e : Exception){
                AptLog.info("process find exception=$e ")
            }
        }
        AapMetas(aapContext).addMetasCode(apiImpls).assembleCode()
        //将所有的类进行打包创建一个新的类进行容纳
        AptLog.info("process end******************************************")
        return false
    }

    override fun getSupportedAnnotationTypes(): Set<String?> {
        return setOf(AapImpl::class.java.canonicalName)
    }

    override fun getSupportedOptions(): Set<String> {
        return mutableSetOf<String>().apply {
            add(AapOptions.DEBUG_OPTION)
            add(AapOptions.BUNDLE_OPTION)
        }
    }
}