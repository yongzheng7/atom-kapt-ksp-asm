package com.atom.plugin.core.ext

import org.objectweb.asm.tree.VarInsnNode

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtVarInsnNode
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/17
 * @Copyright: 2022/2/17 www.rongdasoft.com Inc. All rights reserved.
 */

fun VarInsnNode.println(): String{
    return "VarInsnNode [ var=${this.`var`}, opcode=${this.opcode}, invisibleTypeAnnotations=${this.invisibleTypeAnnotations}, visibleTypeAnnotations=${this.visibleTypeAnnotations} ]"
}