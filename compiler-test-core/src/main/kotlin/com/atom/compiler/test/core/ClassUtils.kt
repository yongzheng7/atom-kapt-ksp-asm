package com.atom.compiler.test.core

import java.io.File
import java.util.regex.Pattern

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ClassUtils
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/24
 * @Copyright: 2022/2/24 www.rongdasoft.com Inc. All rights reserved.
 */
internal object ClassUtils {

    fun getClassFilePath(clazz: Class<*>): String {
        return getClassFile(clazz).absolutePath
    }

    fun getClassFile(clazz: Class<*>): File {
        val dir = clazz.protectionDomain.codeSource.location.file
        val name = clazz.simpleName + ".class"
        return File(
            dir + Pattern.compile("[.]").matcher(clazz.`package`.name).replaceAll("/") + "/", name
        )
    }
}