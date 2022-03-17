package com.atom.compiler.apt.common

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AptContext
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/9
 * @Copyright: 2022/3/9 www.rongdasoft.com Inc. All rights reserved.
 */
object AptContext {
    lateinit var types: Types
    lateinit var elements: Elements
    lateinit var messager: Messager
    lateinit var filer: Filer

    fun init(env: ProcessingEnvironment) {
        this.elements = env.elementUtils
        this.types = env.typeUtils
        this.messager = env.messager
        this.filer = env.filer
    }
}