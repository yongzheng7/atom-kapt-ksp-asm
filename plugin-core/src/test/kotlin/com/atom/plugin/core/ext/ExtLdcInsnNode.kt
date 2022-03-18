package com.atom.plugin.core.ext

import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtLdcInsnNode
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/17
 * @Copyright: 2022/2/17 www.rongdasoft.com Inc. All rights reserved.
 */

fun LdcInsnNode.println(): String{
    //
    return "LdcInsnNode [ cst=${this.cst}, type=${this.type}, invisibleTypeAnnotations=${this.invisibleTypeAnnotations}, visibleTypeAnnotations=${this.visibleTypeAnnotations} ]"
}