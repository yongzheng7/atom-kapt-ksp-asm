package com.atom.plugin.core

import com.atom.plugin.core.ext.println
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
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

class Test_Delete_Code {

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
    fun test_delete() {
        val readFileToByteArray = FileUtils.readFileToByteArray(inFile)
        val reader = ClassReader(readFileToByteArray)
        println(" >reader ${reader.className} ${reader.superName}")
        val node = ClassNode()
        reader.accept(node, ClassReader.EXPAND_FRAMES)
        node.methods.forEach { methodNode ->
            val instructions = methodNode.instructions
            var next = instructions.first
            while (next != null) {
                printNode(methodNode ,next )
                if (next is MethodInsnNode) {
                    if (next.owner.equals("android/util/Log")) {
                        removeLog3(methodNode, next)
                        break
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


    //func02 LabelNode [ label=Label [ info=null ], opcode=-1, type=8, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    // 删除 以下
    //func02 LineNumberNode [ line=30, opcode=org.objectweb.asm.tree.LabelNode@19309264, type=15, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    //func02 LdcInsnNode [ cst=Demo, type=9, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    //func02 LdcInsnNode [ cst=func2 1, type=9, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    //func02 MethodInsnNode [ name=e, owner=android/util/Log, desc=(Ljava/lang/String;Ljava/lang/String;)I, itf=false}, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    //func02 InsnNode [ type=0, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    //func02 LabelNode [ label=Label [ info=null ], opcode=-1, type=8, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    // 删除 以上
    //func02 LineNumberNode [ line=31, opcode=org.objectweb.asm.tree.LabelNode@6df9311c, type=15, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    //func02 InsnNode [ type=0, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    //func02 LabelNode [ label=Label [ info=null ], opcode=-1, type=8, invisibleTypeAnnotations=null, visibleTypeAnnotations=null ]
    // 移除代码,需要找到MethodInsnNode,之后找到MethodInsnNode 之上的LineNumberNode,
    // 然后从LineNumberNode删除到下一个LineNumberNode之前,即可删除指定一行代码
    fun removeLog3(methodNode: MethodNode, node: MethodInsnNode) {
        var previous : AbstractInsnNode? = node
        while (previous != null){
            if (previous is LabelNode) {
                break
            }
            previous = previous.previous
        }

        var next : AbstractInsnNode? = previous?.next
        while (next != null){
            if(next is LabelNode){
                break
            }
            next = next.next
            methodNode.instructions.remove(next.previous)
        }
        methodNode.instructions.remove(next)
    }

    fun printNode(methodNode: MethodNode, next: AbstractInsnNode , tag : String ="") {
        when (next) {
            is FieldInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is FrameNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is IincInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is InsnNode -> {
                println("$tag ${methodNode.name} ${next.println()}")
            }
            is IntInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is InvokeDynamicInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is JumpInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is LookupSwitchInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is MultiANewArrayInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is TableSwitchInsnNode -> {
                println("$tag ${methodNode.name} * $next")
            }
            is VarInsnNode -> {
                println("$tag ${methodNode.name} ${next.println()}")
            }
            is LabelNode -> {
                println("$tag ${methodNode.name} ${next.println()}")
            }
            is LdcInsnNode -> {
                println("$tag ${methodNode.name} ${next.println()}")
            }
            is LineNumberNode -> {
                println("$tag ${methodNode.name} ${next.println()}")
            }
            is MethodInsnNode -> {
                println("$tag ${methodNode.name} ${next.println()}")
            }
            is TypeInsnNode -> {
                println("$tag ${methodNode.name} ${next.println()}")
            }
        }
    }
}