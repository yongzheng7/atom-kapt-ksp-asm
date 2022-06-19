package com.atom.plugin.core

import com.atom.plugin.core.ext.println
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.*
import java.io.File

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: TestClass
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/16
 * @Copyright: 2022/2/16 www.rongdasoft.com Inc. All rights reserved.
 */

//https://zhuanlan.zhihu.com/p/401217850
// https://www.cnblogs.com/dawabigbaby/p/12348956.html
// https://blog.csdn.net/lmj623565791/article/details/119880194
// https://zhuanlan.zhihu.com/p/94498015
// ASM ByteCode Outline
// https://blog.csdn.net/ljz2016/article/details/83345673  方法修改以及创建
// https://vimsky.com/examples/detail/java-attribute-org.objectweb.asm.Opcodes.IRETURN.html

class Test_Insert_Code {

    lateinit var inFile: File
    lateinit var outFile: File

    @Before
    fun before() {
//        inFile =
//            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\module-core\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\module\\core\\aap\\AapEngine.class")
//        outFile =
//            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\module-core\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\module\\core\\aap\\AapEngine2.class")
//
//        inFile =
//            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\bytecode\\MainActivity.class")
//        outFile =
//            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\bytecode\\MainActivity2.class")

        // ASMCode.class
        inFile =
            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\bytecode\\ASMCode.class")
        outFile =
            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\bytecode\\ASMCode2.class")

//        inFile =
//            File("D:\\project\\testkaptksp\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\test\\annotation\\Demo.class")
//        outFile =
//            File("D:\\project\\testkaptksp\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\test\\annotation\\Demo2.class")

        if (outFile.exists()) {
            outFile.delete()
        }
    }


    @Test
    fun addCode() {
        val readFileToByteArray = FileUtils.readFileToByteArray(inFile)
        val classReader = ClassReader(readFileToByteArray)
        val classWriter = ClassWriter(classReader, 0)
        val classVisitor: ClassVisitor = SimpleClassVisitor("ASMCode", Opcodes.ASM5, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        FileUtils.writeByteArrayToFile(outFile, classWriter.toByteArray())
        println(" >" + outFile.absolutePath)
    }

    //visit visitSource? visitOuterClass? ( visitAnnotation |
    //   visitAttribute )*
    //   ( visitInnerClass | visitField | visitMethod )*
    //   visitEnd
    // 相关调用顺序如上，?代表这个方法可能不会调用，*标识可能会调用 0 次或者多次
    class SimpleClassVisitor(val className: String, api: Int, cv: ClassVisitor) :
        ClassVisitor(api, cv) {

        override fun visitMethod(
            access: Int, // 标志位 1 override /17 default / 18 private / 20 protected
            name: String?, // 方法名称
            descriptor: String?, // 形参和返回 ()Ljava/lang/String; 雷同 jni注册方法
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            println("SimpleClassVisitor > visitMethod > access=${access}, name=${name}, descriptor=${descriptor}, signature=${signature}, exceptions=${exceptions?.println()},")
            name ?: return visitMethod
            if (name.startsWith("asm")) {
                visitMethod = SimpleMethodVisitor(className, name, Opcodes.ASM5, visitMethod)
            }
            return visitMethod
        }
    }

    //visitAnnotationDefault?
    //(visitAnnotation |visitParameterAnnotation |visitAttribute )* ( visitCode
    //(visitTryCatchBlock |visitLabel |visitFrame |visitXxxInsn | visitLocalVariable |visitLineNumber )*
    //visitMaxs )? visitEnd
    class SimpleMethodVisitor(
        val className: String,
        val name: String,
        api: Int,
        mv: MethodVisitor
    ) :
        MethodVisitor(api, mv), Opcodes {
        // 属于方法的开始
        override fun visitCode() {
            super.visitCode()
            println("SimpleMethodVisitor [${name}] > visitCode")
            addLogger(true)
        }

        override fun visitInsn(opcode: Int) {
            println("SimpleMethodVisitor [${name}] > visitInsn ${opcode}")
            //generate code before return
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                // test_addTryAndCatchCode()
                addLogger(false)
            }
            super.visitInsn(opcode)
        }

        // 属于方法的结束
        override fun visitEnd() {
            println("SimpleMethodVisitor [${name}] > visitEnd ")
            addLog()
            super.visitEnd()
        }

        fun addLogger(isStart: Boolean) {
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                "com/atom/module/logger/Logger",
                "Forest",
                "Lcom/atom/module/logger/Logger\$Forest;"
            )
            mv.visitInsn(Opcodes.ICONST_3)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
            mv.visitVarInsn(Opcodes.ASTORE, 3)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitLdcInsn(className)
            mv.visitInsn(Opcodes.AASTORE)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitLdcInsn(name)
            mv.visitInsn(Opcodes.AASTORE)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitInsn(Opcodes.ICONST_2)
            mv.visitLdcInsn(if (isStart) "start--------------------->" else "end---------------------<")
            mv.visitInsn(Opcodes.AASTORE)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/atom/module/logger/Logger\$Forest",
                "i",
                "([Ljava/lang/Object;)V",
                false
            )
        }

        fun addLog() {
            mv.visitLdcInsn("system")
            mv.visitLdcInsn("test")
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "android/util/Log",
                "e",
                "(Ljava/lang/String;Ljava/lang/String;)I",
                false
            )
            mv.visitInsn(Opcodes.POP)
        }

        fun test_addTryAndCatchCode() {
            addTryAndCatchCode(tryBlockCode = { m ->
                m.visitLdcInsn("system")
                m.visitLdcInsn("test")
                m.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "android/util/Log",
                    "e",
                    "(Ljava/lang/String;Ljava/lang/String;)I",
                    false
                )
                m.visitInsn(Opcodes.POP)
            }, catchBlockCode = { m, e ->
                m.visitLdcInsn(e)
                m.visitLdcInsn("test")
                m.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "android/util/Log",
                    "e",
                    "(Ljava/lang/String;Ljava/lang/String;)I",
                    false
                )
                m.visitInsn(Opcodes.POP)
            })
        }

        fun addTryAndCatchCode(
            exception: String = "java/lang/Exception",
            tryBlockCode: (MethodVisitor) -> Unit,
            catchBlockCode: (MethodVisitor, String) -> Unit = { _, _ -> }
        ) {
            val label0 = Label()
            val label1 = Label()
            val label2 = Label()
            val label3 = Label()
            val label4 = Label()
            mv.visitTryCatchBlock(label0, label1, label2, exception)
            mv.visitLabel(label0)
            tryBlockCode(mv)
            mv.visitLabel(label1)
            mv.visitJumpInsn(Opcodes.GOTO, label3)
            mv.visitLabel(label2)
            mv.visitFrame(
                Opcodes.F_SAME1,
                0,
                null,
                1,
                arrayOf<Any>(exception)
            )
            mv.visitVarInsn(Opcodes.ASTORE, 1)
            mv.visitLabel(label4)
            catchBlockCode(mv, "e")
            mv.visitLabel(label3)
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
            mv.visitLocalVariable("e", "L${exception};", null, label4, label3, 1)
        }

        fun addSystemOut() {
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
            )
            mv.visitLdcInsn("adasda")
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "printf",
                "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;",
                false
            )
            mv.visitInsn(Opcodes.POP)
        }

        fun addAndroidLog() {
            //将类文件的路径转化为包的路径
            //val name = nameFinal.replaceAll("/", ".")
            //访问方法的参数--搜索到的接口类名
            mv.visitLdcInsn("name1")
            mv.visitLdcInsn("name2")
            // 生成注册代码到 LogisticsCenter.loadRouterMap() 方法中
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC   //操作码
                , "android/util/Log" //访问类的类名
                , "e" //访问的方法
                , "(Ljava/lang/String;Ljava/lang/String;)V"   //访问参数的类型
                , false
            )   //访问的类是否是接口
            mv.visitInsn(Opcodes.POP)
        }
    }

    @Test
    fun `kotlin  添加代码到object 中 也是添加到class的实体中 和添加静态方法有区别`() {
        val readFileToByteArray = FileUtils.readFileToByteArray(inFile)
        val classReader = ClassReader(readFileToByteArray)
        val classWriter = ClassWriter(classReader, 0)
        val classVisitor: ClassVisitor = SingleClassVisitor(Opcodes.ASM5, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        FileUtils.writeByteArrayToFile(outFile, classWriter.toByteArray())
        println(" >" + outFile.absolutePath)
    }

    class SingleClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {
        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            //generate code into this method
            if (name == "loadProxyClass") { //找到动态生成注册代码需要注入的 loadRouterMap 方法
                println("transformJar AapImplClassVisitor ${name}")
                mv = SingleMethodVisitor(Opcodes.ASM5, mv)
            }
            return mv
        }
    }

    class SingleMethodVisitor(api: Int, cv: MethodVisitor) : MethodVisitor(api, cv) {
        override fun visitInsn(opcode: Int) {
            //generate code before return
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                println("transformJar SingleMethodVisitor ${opcode}")
                // 生成注册代码到 LogisticsCenter.loadRouterMap() 方法中
                // https://blog.csdn.net/kangkanglou/article/details/79422520
                mv.visitCode()
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitLdcInsn("sdasdasdasdasdasd")
                mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "com/atom/module/core/aap/AapEngine",
                    "registerClass",
                    "(Ljava/lang/String;)V",
                    false
                )

            }
            super.visitInsn(opcode)
        }
    }

}