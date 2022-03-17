package com.atom.plugin.core.test.aap

import com.atom.plugin.core.Log
import com.atom.plugin.core.ext.replaceAll
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import org.objectweb.asm.*

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: RegisterCodeGenerator
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/15
 * @Copyright: 2022/2/15 www.rongdasoft.com Inc. All rights reserved.
 */
class RegisterCodeGenerator(val extension: ScanSet) {

    companion object {
        /**
         * 插入路由注册代码
         * @param registerSetting 扫描到的注册内容
         */
        fun insertInitCodeTo(destClassFile: File, registerSetting: ScanSet) {
            if (registerSetting.classList.isNotEmpty()) {
                val processor = RegisterCodeGenerator(registerSetting)
                if (destClassFile.name.endsWith(".jar")) {
                    processor.insertInitCodeIntoJarFile(destClassFile)
                }
            }
        }
    }

    /**
     * 遍历jar包找到 LogisticsCenter.class 文件，向其中加入注册的代码
     * @param jarFile the jar file which contains LogisticsCenter.class
     * @return
     */
    fun insertInitCodeIntoJarFile(jarFile: File): File {
        // 创建一个 待插入代码的class  AtomApi.class
        val optJar = File(jarFile.parent, jarFile.name + ".opt")
        if (optJar.exists()) {
            optJar.delete()
        }
        // AtomApi.class 所在的jar包
        val file = JarFile(jarFile)
        val enumeration = file.entries()
        val jarOutputStream = JarOutputStream(FileOutputStream(optJar))

        //遍历jar包，找到 LogisticsCenter.class 文件
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement() as JarEntry
            val entryName = jarEntry.name
            val zipEntry = ZipEntry(entryName)
            val inputStream = file.getInputStream(jarEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                Log.e("Insert init code to class >>$entryName")
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
        return jarFile
    }

    /**
     * 访问class类，动态添加执行方法代码
     * @param inputStream
     * @return
     */
    fun referHackWhenInit(inputStream: InputStream): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv: ClassVisitor = MyClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    inner class MyClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {

        override fun visitMethod(
            access: Int,
            name: String?,
            desc: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var mv = super.visitMethod(access, name, desc, signature, exceptions)
            // 判断方法名称是否为指定的名称
            if (name == ScanSetting.GENERATE_TO_METHOD_NAME) {
                mv = RouteMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }

    }
    // https://zhuanlan.zhihu.com/p/370271069 从ASM入门字节码增强
    //https://blog.csdn.net/wzy1935/article/details/115631556 类的创建和修改 —— ClassWriter的综合应用
    // http://www.blogjava.net/DLevin/archive/2014/06/25/414292.html 深入ASM源码之ClassReader、ClassVisitor、ClassWriter
    inner class RouteMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv) {
        override fun visitInsn(opcode: Int) {
            //generate code before return
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                extension.classList.forEach { nameFinal ->
                    //将类文件的路径转化为包的路径
                    val name = nameFinal.replaceAll("/", ".")
                    //访问方法的参数--搜索到的接口类名
                    mv.visitLdcInsn(name)
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