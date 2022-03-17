package com.atom.plugin.core.ext

import com.atom.plugin.core.ext.println
import org.objectweb.asm.tree.LineNumberNode

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtLineNumberNode
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/17
 * @Copyright: 2022/2/17 www.rongdasoft.com Inc. All rights reserved.
 */

fun LineNumberNode.println(): String{
     return "LineNumberNode [ line=${this.line}, opcode=${this.start.println()}, type=${this.type}, invisibleTypeAnnotations=${this.invisibleTypeAnnotations}, visibleTypeAnnotations=${this.visibleTypeAnnotations} ]"
}