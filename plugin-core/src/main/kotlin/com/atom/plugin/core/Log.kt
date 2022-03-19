package com.atom.plugin.core

import org.gradle.api.Project
import org.gradle.api.logging.Logger

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: Log
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/1/20
 * @Copyright: 2022/1/20 www.rongdasoft.com Inc. All rights reserved.
 */
object Log {
    var logger: Logger? = null
    fun init(project: Project) {
        logger = project.logger
    }

    fun i(info: String?) {
        logger?.info("> Task :ASM :logI :$info")
    }

    fun e(info: String?) {
        logger?.error("> Task :ASM :logE :$info")
    }

    fun w(info: String?) {
        logger?.warn("> Task :ASM :logW :$info")
    }

}