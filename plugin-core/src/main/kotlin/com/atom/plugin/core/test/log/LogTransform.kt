package com.atom.plugin.core.test.log

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.atom.plugin.core.Log
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * 简单的日志
 */
class LogTransform(val project: Project) : Transform() {

    override fun getName(): String {
        Log.e("LogTransform getName() > ")
        return "KotlinLogTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        Log.e("LogTransform getInputTypes() >")
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        Log.e("LogTransform getScopes() >")
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        Log.e("LogTransform isIncremental() > ")
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        Log.e("LogTransform transform() start >")
        val startTime = System.currentTimeMillis()
        Log.e("|---------- LogTransform transform start ------------------------------------->>")
        // 测试打印inputType
        Log.e("|---------- getInputTypes start -->>")
        Log.e("|inputs?.forEach -->>")
        transformInvocation?.inputs?.forEach { it ->
            Log.e("|---------- jarInputs -->>")
            it.jarInputs.forEach { jarInput ->
                Log.e("| [${jarInput.name}]")
                val src = jarInput.file
                val dest = transformInvocation.outputProvider.getContentLocation(
                    jarInput.name,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                Log.e("|jarInput src=[${src.absolutePath} , dest=[${dest.absolutePath}]")
                FileUtils.copyFile(src, dest)
            }
            Log.e("|---------- directoryInputs -->>")
            it.directoryInputs.forEach { directoryInput ->
                val src = directoryInput.file
                val dest = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                Log.e("|directoryInputs src=[${src.absolutePath} , dest=[${dest.absolutePath}]")
                //FileUtils.copyDirectory(src, dest)
                FileUtils.copyDirectoryToDirectory(src, dest)
            }
        }
        Log.e("|---------- getInputTypes  end --->>")

        Log.e("|---------- LogTransform transform  end [${System.currentTimeMillis() - startTime}ms] ------------------------------>>")
        super.transform(transformInvocation)
        Log.e("LogTransform transform() end >")
    }
}