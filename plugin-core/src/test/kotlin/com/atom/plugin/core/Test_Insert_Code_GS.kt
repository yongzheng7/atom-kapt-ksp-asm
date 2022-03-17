package com.atom.plugin.core

import com.atom.plugin.core.ext.println
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
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

class Test_Insert_Code_GS {

    lateinit var inFile: File
    lateinit var outFile: File

    @Before
    fun before() {
        inFile =
            File("D:\\project\\testkaptksp\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\test\\annotation\\Demo.class")
        outFile =
            File("D:\\project\\testkaptksp\\app\\build\\tmp\\kotlin-classes\\debug\\com\\atom\\test\\annotation\\Demo2.class")
    }

    //https://zhuanlan.zhihu.com/p/401217850
    // https://www.cnblogs.com/dawabigbaby/p/12348956.html
    @Test
    fun test() {
        val readFileToByteArray = FileUtils.readFileToByteArray(inFile)
        val reader = ClassReader(readFileToByteArray)
        println(" >reader ${reader.className} ${reader.superName}")
        val node = ClassNode()
        reader.accept(node, ClassReader.EXPAND_FRAMES)
        node.methods.forEach { methodNode ->
            val instructions = methodNode.instructions
            var next = instructions.first
            while (next != null) {
                when (next) {
                    is FieldInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is FrameNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is IincInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is InsnNode -> {
                        println("${methodNode.name} ${next.println()}")
                    }
                    is IntInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is InvokeDynamicInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is JumpInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is LookupSwitchInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is MultiANewArrayInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is TableSwitchInsnNode -> {
                        println("${methodNode.name} * $next")
                    }
                    is VarInsnNode -> {
                        println("${methodNode.name} ${next.println()}")
                    }
                    is LabelNode -> {
                        println("${methodNode.name} ${next.println()}")
                    }
                    is LdcInsnNode -> {
                        println("${methodNode.name} ${next.println()}")
                    }
                    is LineNumberNode -> {
                        println("${methodNode.name} ${next.println()}")
                    }
                    is MethodInsnNode -> {
                        println("${methodNode.name} ${next.println()}")
                    }
                    is TypeInsnNode -> {
                        println("${methodNode.name} ${next.println()}")
                    }
                }
                next = next.next
            }
        }
        val writer = ClassWriter(1)
        node.accept(writer)
        if (outFile.exists()) {
            FileUtils.forceDelete(outFile)
        }
        FileUtils.writeByteArrayToFile(outFile, writer.toByteArray())
        println(" >" + outFile.absolutePath)
    }

    @Test
    fun addCode() {
        val readFileToByteArray = FileUtils.readFileToByteArray(inFile)
        val classReader = ClassReader(readFileToByteArray)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val classVisitor: ClassVisitor = SimpleClassVisitor(Opcodes.ASM5, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        FileUtils.writeByteArrayToFile(outFile, classWriter.toByteArray())
        println(" >" + outFile.absolutePath)
    }

    // https://blog.csdn.net/lmj623565791/article/details/119880194
    // https://zhuanlan.zhihu.com/p/94498015
    // ASM ByteCode Outline
    @Test
    fun addCode2() {
        val classFilePath = ClassUtils.getClassFilePath(Test_Delete_Code::class.java)
        println(" >$classFilePath")
    }

    class SimpleClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {

        override fun visitMethod(
            access: Int, // 标志位 1 override /17 default / 18 private / 20 protected
            name: String?, // 方法名称
            descriptor: String?, // 形参和返回 ()Ljava/lang/String; 雷同 jni注册方法
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            var visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            println(" access=${access}, name=${name}, descriptor=${descriptor}, signature=${signature}, exceptions=${exceptions?.println()},")
            visitMethod = SimpleMethodVisitor(Opcodes.ASM5, visitMethod)
            return visitMethod
        }

        override fun visitField(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            value: Any?
        ): FieldVisitor {
            println(" access=${access}, name=${name}, descriptor=${descriptor}, signature=${signature}, value=${value},")
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
            println(" version=${version}, access=${access}, name=${name}, signature=${signature}, superName=${superName},interfaces=${interfaces?.println()},")
            super.visit(version, access, name, signature, superName, interfaces)
        }
    }

    // https://blog.csdn.net/ljz2016/article/details/83345673  方法修改以及创建
    // https://vimsky.com/examples/detail/java-attribute-org.objectweb.asm.Opcodes.IRETURN.html
    class SimpleMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv), Opcodes {

        override fun visitInsn(opcode: Int) {
            //generate code before return
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                addLogReturn()
            }
            super.visitInsn(opcode)
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

        fun addLogReturn(){
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

        fun v() {
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
        }

//        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
//            super.visitMaxs(maxStack + 4, maxLocals)
//        }
    }
}