package com.atom.plugin.core.ext

import groovy.io.FileType
import java.io.File
import java.io.FileNotFoundException

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: FileExt
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/15
 * @Copyright: 2022/2/15 www.rongdasoft.com Inc. All rights reserved.
 */

fun eachFileRecurse(self: File, fileType: FileType, block: (File) -> Unit) {
    checkDir(self)
    self.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            if (fileType != FileType.FILES) {
                block.invoke(file)
            }
            eachFileRecurse(file, fileType, block)
        } else if (fileType != FileType.DIRECTORIES) {
            block.invoke(file)
        }
    }
}

fun checkDir(self: File) {
    if (!self.exists()) {
        throw FileNotFoundException(self.absolutePath)
    } else require(self.isDirectory) { "The provided File object is not a directory: " + self.absolutePath }
}