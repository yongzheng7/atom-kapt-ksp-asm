package com.atom.plugin.core

import com.atom.plugin.core.ext.printString

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AbstractExtension
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/15
 * @Copyright: 2022/3/15 www.rongdasoft.com Inc. All rights reserved.
 */
// TODO 继承该类的子类 一律都需要设置为public 或者 kotlin里的 open
abstract class AbstractExtension {

    var enableUse: Boolean = true

    var enableLog: Boolean = true

    var runSingle: Boolean = false

    var include: Array<String>? = null

    var exclude: Array<String>? = null

    override fun toString(): String {
        return "AbstractExtension(enableUse=$enableUse, enableLog=$enableLog, runSingle=$runSingle, include=${include?.printString()}, exclude=${exclude?.printString()})"
    }
}