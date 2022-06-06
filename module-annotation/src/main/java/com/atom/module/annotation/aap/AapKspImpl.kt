package com.atom.module.annotation.aap

import kotlin.reflect.KClass

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AapImpl
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/1/19
 * @Copyright: 2022/1/19 www.rongdasoft.com Inc. All rights reserved.
 */
@MustBeDocumented
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AapKspImpl(val api: KClass<*>, val name: String = "", val version: Long = 0)
