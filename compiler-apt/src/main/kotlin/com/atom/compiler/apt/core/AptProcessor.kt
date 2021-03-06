package com.atom.compiler.apt.core

import javax.annotation.processing.*
import javax.lang.model.SourceVersion

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AapProcessor
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/9
 * @Copyright: 2022/3/9 www.rongdasoft.com Inc. All rights reserved.
 */

abstract class AptProcessor : AbstractProcessor() {
    final override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv?.also {
            AptContext.init(it)
            AptLog.init(it.messager, true)
            initOptions(AptContext, it.options)
        }
    }

    abstract fun initOptions(context: AptContext, options: Map<String, String>)

    override fun getSupportedSourceVersion(): SourceVersion? {
        return SourceVersion.latestSupported()
    }
}