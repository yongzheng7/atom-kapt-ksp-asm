package com.atom.compiler.apt

import com.atom.compiler.apt.common.AptContext
import com.atom.compiler.apt.common.AptLog
import com.atom.module.aap.annotation.AapImpl
import com.google.auto.service.AutoService
import java.util.HashSet
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AapProcessor
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/9
 * @Copyright: 2022/3/9 www.rongdasoft.com Inc. All rights reserved.
 */
@AutoService(Processor::class)
class AapProcessor : AbstractProcessor() {

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)

        processingEnv?.also {
            AptContext.init(it)
            AptLog.init(it.messager, false)
        }
        println("AapProcessor ->>>>>> init")
        AptLog.info("AapProcessor ->>>>>> init")
        AptLog.warning("AapProcessor ->>>>>> init")
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {

        println("AapProcessor ->>>>>> process")
        AptLog.warning("AapProcessor ->>>>>> process")
        AptLog.info("AapProcessor ->>>>>> process")
        return false
    }

    override fun getSupportedSourceVersion(): SourceVersion? {
        return SourceVersion.latestSupported()
    }


    override fun getSupportedAnnotationTypes(): Set<String?> {
        return mutableSetOf<String>().apply {
            this.add(AapImpl::class.java.canonicalName)
        }
    }

    override fun getSupportedOptions(): Set<String> {
        //options.add(Consts.DEBUG_OPTION)
        //options.add(Consts.BUNDLE_CLASSNAME)
        return mutableSetOf<String>().apply {

        }
    }


}