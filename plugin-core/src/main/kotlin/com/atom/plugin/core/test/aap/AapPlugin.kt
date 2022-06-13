package com.atom.plugin.core.test.aap

import com.android.build.api.transform.TransformInvocation
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

    val GENERATE_TO_CLASS_NAME = "com/atom/module/core/aap/AapEngine"
    val GENERATE_TO_CLASS_FILE_NAME = "$GENERATE_TO_CLASS_NAME.class"
    val GENERATE_TO_METHOD_NAME = "loadProxyClass"
    val ROUTER_CLASS_PACKAGE_NAME = "com\\atom\\apt\\proxy"
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
        return false
    }

    override fun canTransForm(name: String): Boolean {
        if (name.contains("com/google/android")) return false
        if (name.contains("androidx/constraintlayout")) return false
        if (name.contains("androidx/core")) return false
        if (name.contains("androidx/drawerlayout")) return false
        if (name.contains("androidx/viewpager2")) return false
        return super.canTransForm(name)
    }

    override fun transformDir(classBytes: ByteArray, inputFile: File, outputFile: File): ByteArray {
        Log.e("transformDir \n inputFile = ${inputFile.absolutePath} \n outputFile = ${inputFile.absolutePath}")
        if (inputFile.isFile
            && inputFile.absolutePath.contains(ROUTER_CLASS_PACKAGE_NAME)) {
            Log.e("transformDir 找到指定的需要搜集的类 ${inputFile.name}")
            return scanning(classBytes)
        }
        return classBytes
    }

    override fun transformJar(
        classBytes: ByteArray,
        entry: JarEntry,
        inputFile: File,
        outputFile: File
    ): ByteArray {
        Log.e("transformDir \n ${entry.name} \n inputFile = ${inputFile.absolutePath} \n outputFile = ${inputFile.absolutePath}")

        if (GENERATE_TO_CLASS_FILE_NAME == entry.name) {
            Log.e("transformJar 找到指定的需要插入的ApiImpl类的 ${entry.name}")
            scanningInsertResultFileClass = outputFile
        } else if (entry.name.startsWith(ROUTER_CLASS_PACKAGE_NAME)) {
            Log.e("transformJar 找到指定的需要搜集的类 ${entry.name}")
            return scanning(classBytes)
        }
        return classBytes
    }

    override fun afterTransform(transformInvocation: TransformInvocation, e: AapExtension) {
        super.afterTransform(transformInvocation, e)
        Log.e("afterEvaluate ${scanningInsertResultFileClass?.let { "success" } ?: let { "failure" }}\n scanningResultList = ${scanningResultList} , \n scanningInsertResultClass = $scanningInsertResultFileClass")
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
                        if (GENERATE_TO_CLASS_FILE_NAME == entryName) {
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
            Log.e("transformJar extension != null")
            it.registerList.forEach { entry ->
                if (entry.isInterface()) {
                    reader.interfaces.iterator().forEach { interfaceName ->
                        if (entry.name == interfaceName) {
                            Log.e("transformJar 找到一个 interfaceClass [${interfaceName}]的类 <|> ${entry.name} <|> ${reader.className}")
                            scanningResultList.add(reader.className)
                        }
                    }
                } else {
                    if (entry.name == reader.superName) {
                        Log.e("transformJar 找到一个 superClass [${reader.superName}]的类 <|> ${entry.name} <|> ${reader.className}")
                        scanningResultList.add(reader.className)
                    }
                }
            }
        } ?:also {
            Log.e("transformJar extension == null")
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
            if (name == GENERATE_TO_METHOD_NAME) { //找到动态生成注册代码需要注入的 loadRouterMap 方法
                Log.e("transformJar AapImplClassVisitor ${name}")
                mv = AapImplMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }
    }

    inner class AapImplMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv) {

        override fun visitInsn(opcode: Int) {
            //generate code before return
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                if(scanningResultList.isNotEmpty()){
                    mv.visitCode()
                    scanningResultList.forEach {
                        Log.e("transformJar AapImplMethodVisitor visitInsn <> ${it}")
                        val name = it.replaceAll("/", ".")  //将类文件的路径转化为包的路径
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitLdcInsn(name) //访问方法的参数--搜索到的接口类名
                        // 生成注册代码到 LogisticsCenter.loadRouterMap() 方法中
                        // https://blog.csdn.net/kangkanglou/article/details/79422520
                        mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL   //操作码
                            , GENERATE_TO_CLASS_NAME //访问类的类名
                            , REGISTER_METHOD_NAME //访问的方法
                            , "(Ljava/lang/String;)V"   //访问参数的类型
                            , false
                        )   //访问的类是否是接口
                    }
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }

}