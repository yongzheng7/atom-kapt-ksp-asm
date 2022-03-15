package com.atom.plugin.core

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import com.sun.org.apache.xpath.internal.operations.Bool
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer

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
    protected var isApp: Boolean = true
    protected var extension: E? = null
    protected var isRun: Boolean = false

    abstract fun getExtensionName(): String

    abstract fun getExtensionClass(): Class<E>

    override fun apply(project: Project) {
        Log.init(project)
        this.project = project
        val plugins: PluginContainer = project.plugins
        val hasAppPlugin: Boolean = plugins.hasPlugin(AppPlugin::class.java)
        hasAppPlugin.isTrue {
            val extensions: ExtensionContainer = project.extensions
            this.isApp = extensions is AppExtension
            this.extension = extensions.create(getExtensionName(), getExtensionClass())
        }
    }

    override fun getName(): String = getExtensionName()

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return if (isApp) {
            TransformManager.SCOPE_FULL_PROJECT
        } else {
            Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT)
        }
    }


}