package com.atom.plugin.core

import com.sun.org.apache.xpath.internal.operations.Bool

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtBoolean
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/15
 * @Copyright: 2022/3/15 www.rongdasoft.com Inc. All rights reserved.
 */
fun Boolean.isTrue(block: () -> Unit): Boolean {
    if (this) {
        block.invoke()
    }
    return this
}

fun Boolean.isFalse(block: () -> Unit): Boolean {
    if (!this) {
        block.invoke()
    }
    return this
}

fun Boolean.check(block: () -> Unit) {

}