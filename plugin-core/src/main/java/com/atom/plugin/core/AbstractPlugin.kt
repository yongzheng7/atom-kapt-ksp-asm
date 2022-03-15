package com.atom.plugin.core

import com.android.build.api.transform.Transform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AbstractPlugin
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/15
 * @Copyright: 2022/3/15 www.rongdasoft.com Inc. All rights reserved.
 */
abstract class AbstractPlugin<E : AbstractExtension> : Transform(), Plugin<Project> {

    protected lateinit var project: Project
    protected var isApp = true
    protected var extension: E? = null

}