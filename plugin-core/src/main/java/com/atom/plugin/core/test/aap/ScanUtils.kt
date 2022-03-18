package com.atom.plugin.core.test.aap

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.jar.JarFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * 扫描 com/xuexiang/xrouter/ 所有的class文件
 * <p>寻找到自动生成的路由注册接口：routers、interceptors、providers</p>
 * <p>接口包括：IRouteRoot、IInterceptorGroup、IProviderGroup</p>
 */
object ScanUtils {

    /**
     * 扫描jar文件
     * @param jarFile 所有被打包依赖进apk的jar文件
     * @param destFile dest file after this transform
     */
    fun scanJar(jarFile: File, destFile: File) {
        val file = JarFile(jarFile)
        val enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            var jarEntry = enumeration.nextElement()
            // 获取jar包中每个 class的名称
            var entryName = jarEntry.name
            // 判断是否是指定的包明
            if (entryName.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)) {
                val inputStream: InputStream = file.getInputStream(jarEntry)
                scanClass(inputStream)
                inputStream.close()
            } else if (ScanSetting.GENERATE_TO_CLASS_FILE_NAME == entryName) {
                // 标记这个jar文件中是否存在 AbstractApiImplContext.class -- 需要动态注入注册代码的类
                // 在扫描完成后,将向 AbstractApiImplContext.class 的loadProxyClass方法中注入注册代码
                RegisterTransform.fileContainsInitClass = destFile
            }
        }
        file.close()
    }

    /**
     * 判断jar文件是否可能注册了路由【android的library可以直接排除】
     * @param jarFilepath jar文件的路径
     */
    fun shouldProcessPreDexJar(jarFilepath: String): Boolean {
        return !jarFilepath.contains("com.android.support")
                && !jarFilepath.contains("/android/m2repository")
    }

    /**
     * 判断扫描的类的包名是否是 annotationProcessor自动生成路由代码的包名：com/xuexiang/xrouter/routes/
     * @param classFilePath 扫描的class文件的路径
     */
    fun shouldProcessClass(classFilePath: String?): Boolean {
        return classFilePath != null && classFilePath.startsWith(ScanSetting.ROUTER_CLASS_PACKAGE_NAME)
    }

    /**
     * 扫描class文件
     * @param file class文件
     */
    fun scanClass(file: File) {
        scanClass(FileInputStream(file))
    }

    /**
     * 扫描class文件
     * @param inputStream 文件流
     */
    fun scanClass(inputStream: InputStream) {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        // 创建一个class并进行过滤根据指定的父类和实现的接口
        val cv = ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    // 该类的 浏览者
    class ScanClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {
        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            super.visit(version, access, name, signature, superName, interfaces)
            // 提前注册进来的 类
            // 如果实现指定的接口,就记录下这个类 名字
            RegisterTransform.registerList.forEach { ext ->
                if (interfaces != null && ext.isInterface()) {
                    interfaces.forEach { itName ->
                        if (itName == ext.name) {
                            // 搜索实现指定接口的类
                            name?.also {
                                ext.classList.add(name)
                            }
                        }
                    }
                }
                if (ext.isSuper() && ext.name == superName) {
                    // 搜索实现继承指定的类
                    name?.also {
                        ext.classList.add(name)
                    }
                }
            }
        }
    }
}