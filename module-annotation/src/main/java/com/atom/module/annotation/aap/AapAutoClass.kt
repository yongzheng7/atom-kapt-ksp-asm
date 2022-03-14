package com.atom.module.aap.annotation

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AapAutoClass
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/1
 * @Copyright: 2022/3/1 www.rongdasoft.com Inc. All rights reserved.
 */
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@kotlin.annotation.Target(
    AnnotationTarget.CLASS ,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.EXPRESSION,
    AnnotationTarget.FILE,
)
annotation class AapAutoClass(
    val value: Array<String>,
    val data: String = "",
    val comments: String = ""
)
