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
    private var logger: Logger? = null
    private var enabled: Boolean = true
    fun init(project: Project) {
        logger = project.logger
    }

    fun setEnable(enabled: Boolean) {
        this.enabled = enabled
    }

    fun i(info: String?) {
        this.enabled.isTrue {
            logger?.info("> Task :ASM :logI :$info")
        }
    }

    fun e(error: String?) {
        this.enabled.isTrue {
            logger?.error("> Task :ASM :logE :$error")
        }
    }

    fun w(warn: String?) {
        this.enabled.isTrue {
            logger?.warn("> Task :ASM :logW :$warn")
        }
    }

}