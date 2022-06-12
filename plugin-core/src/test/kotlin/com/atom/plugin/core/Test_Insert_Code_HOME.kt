package com.atom.plugin.core

import com.atom.plugin.core.ext.println
import com.atom.plugin.core.ext.replaceAll
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

class Test_Insert_Code_HOME {

    lateinit var inFile: File
    lateinit var outFile: File

    @Before
    fun before() {
        inFile =
            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\module-core\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\module\\core\\aap\\AapEngine.class")
        outFile =
            File("D:\\app_git_android\\demo_asm\\test-plugin-compiler\\module-core\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\module\\core\\aap\\AapEngine2.class")

        if(outFile.exists()){
            outFile.delete()
        }
    }


    @Test
    fun addCode() {
        val readFileToByteArray = FileUtils.readFileToByteArray(inFile)
        val classReader = ClassReader(readFileToByteArray)
        val classWriter = ClassWriter(classReader, 0)
        val classVisitor: ClassVisitor = SimpleClassVisitor(Opcodes.ASM5, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        FileUtils.writeByteArrayToFile(outFile, classWriter.toByteArray())
        println(" >" + outFile.absolutePath)
    }
    //visit visitSource? visitOuterClass? ( visitAnnotation |
    //   visitAttribute )*
    //   ( visitInnerClass | visitField | visitMethod )*
    //   visitEnd
    // 相关调用顺序如上，?代表这个方法可能不会调用，*标识可能会调用 0 次或者多次
    class SimpleClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {

        override fun visitMethod(
            access: Int, // 标志位 1 override /17 default / 18 private / 20 protected
            name: String?, // 方法名称
            descriptor: String?, // 形参和返回 ()Ljava/lang/String; 雷同 jni注册方法
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            println("visitMethod > access=${access}, name=${name}, descriptor=${descriptor}, signature=${signature}, exceptions=${exceptions?.println()},")
            //visitMethod = SimpleMethodVisitor(Opcodes.ASM5, visitMethod)
            return visitMethod
        }

        override fun visitField(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            value: Any?
        ): FieldVisitor {
            println("visitField > access=${access}, name=${name}, descriptor=${descriptor}, signature=${signature}, value=${value},")
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            println("visit > version=${version}, access=${access}, name=${name}, signature=${signature}, superName=${superName},interfaces=${interfaces?.println()},")
            super.visit(version, access, name, signature, superName, interfaces)
        }
    }

    //visitAnnotationDefault?
    //(visitAnnotation |visitParameterAnnotation |visitAttribute )* ( visitCode
    //(visitTryCatchBlock |visitLabel |visitFrame |visitXxxInsn | visitLocalVariable |visitLineNumber )*
    //visitMaxs )? visitEnd
    class SimpleMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv), Opcodes {

        override fun visitCode() {
            super.visitCode()
            //addTryCatch_1()
        }

        override fun visitInsn(opcode: Int) {
            //generate code before return
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                test40()
            }
            super.visitInsn(opcode)
        }

        override fun visitEnd() {
            test401()
            super.visitEnd()
        }

        val label0 = Label()
        val label1 = Label()
        val label2 = Label()
        val label3 = Label()
        val label4 = Label()

        fun addTryCatchStart() {
            mv.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception")
            mv.visitLabel(label0)
        }

        fun addTryCatchEnd() {
            mv.visitLabel(label1)

            val label3 = Label()
            mv.visitJumpInsn(Opcodes.GOTO, label3)
            mv.visitLabel(label2)

            mv.visitFrame(
                Opcodes.F_SAME1,
                0,
                null,
                1,
                arrayOf<Any>("java/lang/Exception")
            )
            mv.visitVarInsn(Opcodes.ASTORE, 1)
            mv.visitLabel(label3)
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        }

        fun addTryCatch_1() {
            val label0 = Label()
            val label1 = Label()
            val label2 = Label()
            mv.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception")
            mv.visitLabel(label0)
        }

        fun addTryCatch_2() {
            mv.visitLabel(label1)
            mv.visitJumpInsn(Opcodes.GOTO, label3)
            mv.visitLabel(label2)
            mv.visitFrame(
                Opcodes.F_SAME1,
                0,
                null,
                1,
                arrayOf<Any>("java/lang/Exception")
            )
            mv.visitVarInsn(Opcodes.ASTORE, 1)
            mv.visitLabel(label4)
        }

        fun addTryCatch_3() {
            mv.visitLabel(label3)
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        }

        fun addTryCatch_4() {
            mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, label4, label3, 1)
        }

        fun test43() {

            val label0 = Label()
            val label1 = Label()
            val label2 = Label()
            mv.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception")
            mv.visitLabel(label0)

            addLog()

            mv.visitLabel(label1)

            val label3 = Label()
            mv.visitJumpInsn(Opcodes.GOTO, label3)
            mv.visitLabel(label2)

            mv.visitFrame(
                Opcodes.F_SAME1,
                0,
                null,
                1,
                arrayOf<Any>("java/lang/Exception")
            )
            mv.visitVarInsn(Opcodes.ASTORE, 1)
            mv.visitLabel(label3)
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        }

        fun test41() {
            val label0 = Label()
            val label1 = Label()
            val label2 = Label()
            mv.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception")
            mv.visitLabel(label0)
            addLog()
            mv.visitLabel(label1)
            val label3 = Label()
            mv.visitJumpInsn(Opcodes.GOTO, label3)
            mv.visitLabel(label2)
            mv.visitFrame(
                Opcodes.F_SAME1,
                0,
                null,
                1,
                arrayOf<Any>("java/lang/Exception")
            )
            mv.visitVarInsn(Opcodes.ASTORE, 1)
            val label4 = Label()
            mv.visitLabel(label4)
            mv.visitLineNumber(53, label4)
            addLog()
            mv.visitLabel(label3)

            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitInsn(Opcodes.IRETURN)
            val label5 = Label()
            mv.visitLabel(label5)

            mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, label4, label3, 1)
            mv.visitLocalVariable(
                "this",
                "Lcom/atom/test/asm_java8/Hello;",
                null,
                label0,
                label5,
                0
            )
        }

        fun test40() {
            mv.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception")
            mv.visitLabel(label0)
            addLog()
            mv.visitLabel(label1)
            mv.visitJumpInsn(Opcodes.GOTO, label3)
            mv.visitLabel(label2)
            mv.visitFrame(
                Opcodes.F_SAME1,
                0,
                null,
                1,
                arrayOf<Any>("java/lang/Exception")
            )
            mv.visitVarInsn(Opcodes.ASTORE, 1)
            mv.visitLabel(label4)
            addLog()
            mv.visitLabel(label3)
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        }

        fun test401() {
            mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, label4, label3, 1)
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


//        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
//            super.visitMaxs(maxStack + 4, maxLocals)
//        }
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

    class SingleClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv){
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

    class SingleMethodVisitor(api: Int, cv: MethodVisitor) : MethodVisitor(api, cv){
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
                    false)

            }
            super.visitInsn(opcode)
        }
    }

}