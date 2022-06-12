package com.atom.plugin.core.test.aap

import com.atom.plugin.core.AbstractPlugin
import com.atom.plugin.core.Log
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

    val GENERATE_TO_CLASS_NAME = "com/atom/core/ApiImpl"
    val GENERATE_TO_CLASS_FILE_NAME = "$GENERATE_TO_CLASS_NAME.class"
    val GENERATE_TO_METHOD_NAME = "loadProxyClass"
    val ROUTER_CLASS_PACKAGE_NAME = "com/atom/apt/proxy"
    val REGISTER_METHOD_NAME = "registerClass"

    override fun getExtensionName(): String {
        return this.javaClass.simpleName
    }

    override fun getExtensionClass(): Class<AapExtension> {
        return AapExtension::class.java
    }

    override fun isFilterJar(jarFile: File): Boolean {
        return jarFile.absolutePath.contains("com.android.support")
                || jarFile.absolutePath.contains("/android/m2repository")
    }

    override fun transformDir(classBytes: ByteArray, classFile: File): ByteArray {
        return classBytes
    }

    override fun transformJar(classBytes: ByteArray, entry: JarEntry, jarFile: File): ByteArray {
        Log.e("transformJar ${entry.name}  ${jarFile.absolutePath}")
        if(GENERATE_TO_CLASS_FILE_NAME == entry.name){
            Log.e("transformJar 找到指定的需要插入的ApiImpl类的")
        }
        return classBytes
    }
}