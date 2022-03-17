package com.atom.plugin.core.ext

import org.objectweb.asm.Label

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtLabel
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/17
 * @Copyright: 2022/2/17 www.rongdasoft.com Inc. All rights reserved.
 */
fun Label.println(): String {
    return "Label [ info=${this.info} ]"
}