package com.atom.plugin.clear

import com.atom.plugin.core.AbstractPlugin
import com.atom.plugin.core.Log
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.*
import java.io.File
import java.util.jar.JarEntry


/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: LogPlugin
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/1/20
 * @Copyright: 2022/1/20 www.rongdasoft.com Inc. All rights reserved.
 */

class CodeClearPlugin : AbstractPlugin<CodeClearExtension>() {

    override fun getExtensionName(): String {
        return "codeClearPlugin"
    }

    override fun getExtensionClass(): Class<CodeClearExtension> {
        return CodeClearExtension::class.java
    }

    override fun transform(classBytes: ByteArray, classFile: File): ByteArray {
        val reader = ClassReader(classBytes)
        Log.e("${getExtensionName()} transform > ${reader.className}  ${classFile.absolutePath}")
        val node = ClassNode()
        reader.accept(node, ClassReader.EXPAND_FRAMES)
        if (reader.className.equals("com/atom/bytecode/MainActivity")) {
            node.methods.forEach { methodNode ->
                removeLogLabel(methodNode)
            }
        }
        val writer = ClassWriter(1)
        node.accept(writer)
        return writer.toByteArray()
    }

    override fun transformJar(classBytes: ByteArray, entry: JarEntry, jarFile: File): ByteArray {
        return transform(classBytes, jarFile)
    }


    private fun removeLogLabel(methodNode: MethodNode) {
        try {
            var t = methodNode.instructions.first
            val mustBeRemovedLabel: MutableList<AbstractInsnNode> = arrayListOf()
            while (t != null) {
                if (t is MethodInsnNode) {
                    if (t.owner == "android/util/Log") {
                        //从此往上找lable
                        var find = t
                        var hasLDC = false
                        var hasMethodInsnNode = false
                        var hasTypeInsnNode = false
                        while (find.previous != null) {
                            find = find.previous
                            if (find is LdcInsnNode) {
                                hasLDC = true
                            }
                            if (find is MethodInsnNode) {
                                hasMethodInsnNode = true
                            }
                            if (find is TypeInsnNode
                                && find.desc == "java/lang/StringBuilder"
                            ) {
                                hasTypeInsnNode = true
                            }
                            if (find is LabelNode) {
                                if (hasLDC && !hasMethodInsnNode && !hasTypeInsnNode) {
                                    mustBeRemovedLabel.add(find)
                                    break
                                }
                                if (hasMethodInsnNode && hasTypeInsnNode && hasLDC) {
                                    mustBeRemovedLabel.add(find)
                                    break
                                }
                            }
                        }
                    }
                }
                t = t.next
            }
            for (i in mustBeRemovedLabel.indices) {
                Log.e("\n")
                var item: AbstractInsnNode? = mustBeRemovedLabel[i]
                var findLog = false
                while (item != null) {
                    if (item is MethodInsnNode
                        && item.owner == "android/util/Log"
                    ) {
                        findLog = true
                    }
                    item = item.next
                    if (item.previous !is LabelNode
                        && item.previous !is FrameNode
                        && item.previous !is LineNumberNode
                    ) {
                        Log.e("remove:" + item.previous.javaClass.name + " findLog = " + findLog)
                        methodNode.instructions.remove(item.previous)
                    }
                    if (findLog && item is LabelNode) {
                        break
                    }
                }
            }
            mustBeRemovedLabel.clear()
        } catch (e: Exception) {
            Log.e(e.localizedMessage)
        }
    }
}