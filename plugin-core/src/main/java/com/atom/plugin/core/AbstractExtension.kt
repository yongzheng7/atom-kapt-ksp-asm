package com.atom.plugin.core

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AbstractExtension
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/15
 * @Copyright: 2022/3/15 www.rongdasoft.com Inc. All rights reserved.
 */
abstract class AbstractExtension {

    var enableUse: Boolean = true

    var enableLog: Boolean = true

    var variantRun: Boolean? = null

    var include: MutableList<String> = arrayListOf()

    var exclude: MutableList<String> = arrayListOf()

    override fun toString(): String {
        return "AbstractExtension(enableUse=$enableUse, enableLog=$enableLog, variantRun=$variantRun, include=$include, exclude=$exclude)"
    }
}