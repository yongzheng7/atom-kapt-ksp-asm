package com.atom.plugin.core.ext

import org.objectweb.asm.tree.MethodInsnNode
/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtMethodInsnNode
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/17
 * @Copyright: 2022/2/17 www.rongdasoft.com Inc. All rights reserved.
 */

fun MethodInsnNode.println(): String{
    //                             方法名称     所属类{java/lang/Object}  方法形参[()V]      所属类是否未接口类型
    return "MethodInsnNode [ name=${this.name}, owner=${this.owner}, desc=${this.desc}, itf=${this.itf}}, invisibleTypeAnnotations=${this.invisibleTypeAnnotations}, visibleTypeAnnotations=${this.visibleTypeAnnotations} ]"
}