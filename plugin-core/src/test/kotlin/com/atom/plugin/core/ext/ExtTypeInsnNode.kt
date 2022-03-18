package com.atom.plugin.core.ext

import org.objectweb.asm.tree.TypeInsnNode

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ExtTypeInsnNode
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/17
 * @Copyright: 2022/2/17 www.rongdasoft.com Inc. All rights reserved.
 */

fun TypeInsnNode.println(): String{
    return "TypeInsnNode [ desc=${this.desc}, invisibleTypeAnnotations=${this.invisibleTypeAnnotations}, visibleTypeAnnotations=${this.visibleTypeAnnotations} ]"
}