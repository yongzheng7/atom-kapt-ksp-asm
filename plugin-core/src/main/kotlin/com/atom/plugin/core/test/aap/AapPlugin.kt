package com.atom.plugin.core.test.aap

import com.atom.plugin.core.AbstractPlugin
import java.io.File
import java.util.jar.JarEntry

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AapPlugin
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/15
 * @Copyright: 2022/2/15 www.rongdasoft.com Inc. All rights reserved.
 */
class AapPlugin : AbstractPlugin<AapExtension>() {
    override fun getExtensionName(): String {
        return "AapPlugin"
    }

    override fun getExtensionClass(): Class<AapExtension> {
        return AapExtension::class.java
    }

    override fun transform(classBytes: ByteArray, classFile: File): ByteArray {
        return classBytes
    }

    override fun transformJar(classBytes: ByteArray, entry: JarEntry, jarFile: File): ByteArray {
        return classBytes
    }
}