package com.atom.plugin.core.ext

import java.lang.StringBuilder

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtArray
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/22
 * @Copyright: 2022/2/22 www.rongdasoft.com Inc. All rights reserved.
 */

fun Array<*>.println(): String{
    val result = StringBuilder()
    result.append('[')
    this.forEachIndexed { index, any ->
        result.append("${index}:${any.toString()},")
    }
    result.append(']')
    return result.toString()
}