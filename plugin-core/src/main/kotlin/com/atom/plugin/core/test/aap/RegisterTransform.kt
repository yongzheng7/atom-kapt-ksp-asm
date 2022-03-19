package com.atom.plugin.core.test.aap


import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.atom.plugin.core.Log
import com.atom.plugin.core.ext.eachFileRecurse
import com.atom.plugin.core.ext.replaceAll
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: RegisterTransform
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/15
 * @Copyright: 2022/2/15 www.rongdasoft.com Inc. All rights reserved.
 */
class RegisterTransform : Transform {

    companion object {
        /**
         * 扫描接口的集合
         */
        val registerList = arrayListOf<SuperScanSet>()

        /**
         * 包含 AbstractApiImplContext 类的jar文件
         */
        var fileContainsInitClass: File? = null
    }

    private val project: Project

    constructor(project: Project) : super() {
        this.project = project
    }


    override fun getName(): String {
        return ScanSetting.PLUGIN_NAME
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 插件扫描的作用域【项目中所有的classes文件】
     * @return
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否是增量编译
     * @return
     */
    override fun isIncremental(): Boolean {
        return true
    }


    override fun transform(transformInvocation: TransformInvocation?) {
        transformInvocation ?: return
        val startTime = System.currentTimeMillis()
        Log.e("RegisterTransform transform start $startTime")
        val leftSlash = File.separator.equals("/")
        transformInvocation.inputs.forEach { transformInput ->
            transformInput.jarInputs.forEach { jarInput ->
                var destName = jarInput.name
                // 重新命名
                val hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    // 如果是.jar结尾，截取之前的字符串
                    destName = destName.substring(0, destName.length - 4)
                }
                // 获取输入的源文件
                val src = jarInput.file
                // 创建输出的源文件
                val dest = transformInvocation.outputProvider.getContentLocation(
                    destName + "_" + hexName, // 预防名字重复
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )

                //进行检查排除不必要的jar包
                if (ScanUtils.shouldProcessPreDexJar(src.absolutePath)) {
                    // 进行遍历jar包中的class
                    ScanUtils.scanJar(src, dest)
                }
                FileUtils.copyFile(src, dest)
            }
            transformInput.directoryInputs.forEach { directoryInput ->
                val dest = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )

                var root: String = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator)) {
                    root += File.separator
                }
                eachFileRecurse(directoryInput.file, FileType.ANY) { file ->
                    var path = file.absolutePath.replace(root, "")
                    if (!leftSlash) {
                        path = path.replaceAll("\\\\", "/")
                    }
                    if (file.isFile && ScanUtils.shouldProcessClass(path)) {
                        ScanUtils.scanClass(file)
                    }
                }
                // copy to dest
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
        Log.e("Scan finish, current cost time ${(System.currentTimeMillis() - startTime)}ms")

        //扫描结束后，将扫描到的路由注册结果自动注入到 LogisticsCenter.class 的loadRouterMap方法中
        fileContainsInitClass?.also { it ->
            registerList.forEach { ext ->
                Log.e("Insert register code to file ${it.absolutePath}")

                if (ext.classList.isEmpty()) {
                    Log.e("No class implements found for interface:${ext.name}")
                } else {
                    ext.classList.forEach {
                        Log.e(it)
                    }
                    RegisterCodeGenerator.insertInitCodeTo(it, ext)
                }
            }
        }

        Log.e("Generate code finish, current cost time: ${(System.currentTimeMillis() - startTime)}ms")
    }
}