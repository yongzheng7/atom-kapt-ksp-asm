package com.atom.plugin.core.ext

import com.atom.plugin.core.ext.println
import org.objectweb.asm.tree.LabelNode

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtLabelNode
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/17
 * @Copyright: 2022/2/17 www.rongdasoft.com Inc. All rights reserved.
 */

fun LabelNode.println(): String {
    return "LabelNode [ label=${this.label.println()}, opcode=${this.opcode}, type=${this.type}, invisibleTypeAnnotations=${this.invisibleTypeAnnotations}, visibleTypeAnnotations=${this.visibleTypeAnnotations} ]"
}