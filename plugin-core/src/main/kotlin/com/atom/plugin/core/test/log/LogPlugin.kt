package com.atom.plugin.core.test.log

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.atom.plugin.core.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: LogPlugin
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/1/20
 * @Copyright: 2022/1/20 www.rongdasoft.com Inc. All rights reserved.
 */

class LogPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        Log.init(project)
        Log.e("Test ASM --> beforeEvaluate")
        project.afterEvaluate {
            Log.e("Test ASM --> afterEvaluate")
        }
        Log.e("Test ASM --> start   ${Date()}")
        Log.e("\n")
        val hasAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        if (hasAppPlugin) {
            Log.e("project.plugins.hasPlugin == ${hasAppPlugin}")
            appPlugin(project )
        }
        Log.e("\n")
        Log.e("Test ASM --> end")
    }


    private fun appPlugin(project: Project ) {
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        Log.e("project.extensions.getByType == ${appExtension}")
        val logTransform = LogTransform(project)
        appExtension.registerTransform(logTransform)
    }
}