package com.atom.plugin.core.ext

import java.util.regex.Pattern

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: StringExt
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/15
 * @Copyright: 2022/2/15 www.rongdasoft.com Inc. All rights reserved.
 */

fun String.replaceAll(regex: String, replacement: String): String {
    return Pattern.compile(regex).matcher(this).replaceAll(replacement)
}