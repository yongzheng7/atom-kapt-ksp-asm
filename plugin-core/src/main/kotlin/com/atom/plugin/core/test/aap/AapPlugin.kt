package com.atom.plugin.core.test.aap

import com.atom.plugin.core.AbstractPlugin
import com.atom.plugin.core.Log
import com.atom.plugin.core.ext.replaceAll
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

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

    val scanningResultList = mutableListOf<String>()
    var scanningInsertResultFileClass: File? = null

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
        Log.e("transformDir ${classFile.name}  ${classFile.absolutePath}")
        if (classFile.isFile && classFile.absolutePath.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)) {
            Log.e("transformDir 找到指定的需要搜集的类 ${classFile.name}")
            return scanning(classBytes)
        }
        return classBytes
    }

    override fun transformJar(classBytes: ByteArray, entry: JarEntry, jarFile: File): ByteArray {
        Log.e("transformJar ${entry.name}  ${jarFile.absolutePath}")
        if (GENERATE_TO_CLASS_FILE_NAME == entry.name) {
            Log.e("transformJar 找到指定的需要插入的ApiImpl类的 ${entry.name}")
            scanningInsertResultFileClass = jarFile
        } else if (entry.name.startsWith(ROUTER_CLASS_PACKAGE_NAME)) {
            Log.e("transformJar 找到指定的需要搜集的类 ${entry.name}")
            return scanning(classBytes)
        }
        return classBytes
    }

    override fun afterEvaluate(project: Project) {
        super.afterEvaluate(project)
        Log.e("afterEvaluate ${scanningInsertResultFileClass?.let { "成功" } ?: let { "失败" }}\n scanningResultList = ${scanningResultList} , \n scanningInsertResultClass = $scanningInsertResultFileClass")
        scanningInsertResultFileClass?.also { jarFile ->
            scanningResultList.forEach { entryInsert ->
                Log.e("afterEvaluate entryInsert = ${entryInsert} ")
            }
            extension?.also {
                if (jarFile.name.endsWith(".jar")) {
                    val optJar = File(jarFile.parent, jarFile.name + ".opt")
                    if (optJar.exists()) {
                        optJar.delete()
                    }
                    val file = JarFile(jarFile)
                    var enumeration = file.entries()
                    val jarOutputStream = JarOutputStream(FileOutputStream(optJar))
                    //遍历jar包，找到 LogisticsCenter.class 文件
                    while (enumeration.hasMoreElements()) {
                        var jarEntry = enumeration.nextElement()
                        val entryName = jarEntry.name
                        val zipEntry = ZipEntry(entryName)
                        val inputStream = file.getInputStream(jarEntry)
                        jarOutputStream.putNextEntry(zipEntry)
                        if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                            Log.e("afterEvaluate Insert init code to class = $entryName ")
                            val bytes = referHackWhenInit(inputStream)
                            jarOutputStream.write(bytes)
                        } else {
                            jarOutputStream.write(IOUtils.toByteArray(inputStream))
                        }
                        inputStream.close()
                        jarOutputStream.closeEntry()
                    }
                    jarOutputStream.close()
                    file.close()

                    if (jarFile.exists()) {
                        jarFile.delete()
                    }
                    optJar.renameTo(jarFile)
                }
            }
        }
    }

    private fun scanning(inputArray: ByteArray): ByteArray {
        val reader = ClassReader(inputArray)
        val node = ClassNode()
        reader.accept(node, ClassReader.EXPAND_FRAMES)
        extension?.also {
            it.registerList.forEach { entry ->
                if (entry.isInterface()) {
                    reader.interfaces.iterator().forEach { interfaceName ->
                        if (entry.name == interfaceName) {
                            Log.e("transformJar 找到一个 interfaceClass [${interfaceName}]的类  ${entry.name} ")
                            scanningResultList.add(entry.name)
                        }
                    }
                } else {
                    if (entry.name == reader.superName) {
                        Log.e("transformJar 找到一个 superClass [${reader.superName}]的类   ${entry.name}")
                        scanningResultList.add(entry.name)
                    }
                }
            }
        }
        val writer = ClassWriter(1)
        node.accept(writer)
        return writer.toByteArray()
    }


    private fun referHackWhenInit(inputStream: InputStream): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = AapImplClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    inner class AapImplClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            //generate code into this method
            if (name == ScanSetting.GENERATE_TO_METHOD_NAME) { //找到动态生成注册代码需要注入的 loadRouterMap 方法
                mv = AapImplMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }
    }

    inner class AapImplMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv) {

        override fun visitInsn(opcode: Int) {
            //generate code before return
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                scanningResultList.forEach { it ->
                    val name = it.replaceAll("/", ".")  //将类文件的路径转化为包的路径
                    mv.visitLdcInsn(name) //访问方法的参数--搜索到的接口类名
                    // 生成注册代码到 LogisticsCenter.loadRouterMap() 方法中
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC   //操作码
                        , ScanSetting.GENERATE_TO_CLASS_NAME //访问类的类名
                        , ScanSetting.REGISTER_METHOD_NAME //访问的方法
                        , "(Ljava/lang/String;)V"   //访问参数的类型
                        , false
                    )   //访问的类是否是接口
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }

}