package com.atom.plugin.logger.visiter


import com.atom.plugin.core.Log
import com.atom.plugin.logger.hook.TargetMethod
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method


class DesugaredClassVisitor: ClassVisitor {

    private val mContext: Context
    private val mLog: Log
    private val mNeedInjectTargetMethods: MutableSet<TargetMethod>

    constructor(
        api: Int,
        cv: ClassVisitor,
        context: Context,
        needInjectTargetMethods: MutableSet<TargetMethod>?
    ) :super(api, cv){
        mContext = context
        mLog = context.getLog()
        mNeedInjectTargetMethods = needInjectTargetMethods ?: mutableSetOf()
    }

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
        return DesugaredMethodVisitor(api, methodVisitor, access, name, desc)
    }

    private fun findTargetMethod(name: String, desc: String): TargetMethod? {
        if (mNeedInjectTargetMethods.isEmpty()) {
            return null
        }
        for (targetMethod in mNeedInjectTargetMethods) {
            if (name == targetMethod.getName() && desc == targetMethod.getDesc()) {
                return targetMethod
            }
        }
        return null
    }

    private inner class DesugaredMethodVisitor(
        api: Int,
        mv: MethodVisitor?,
        access: Int,
        private val mName: String,
        private val mDesc: String
    ) :
        AdviceAdapter(api, mv, access, mName, mDesc) {
        override fun onMethodEnter() {
            val targetMethod: TargetMethod = findTargetMethod(mName, mDesc) ?: return
            for (injectMethod in targetMethod.getInjectMethods()) {
                if (!injectMethod!!.isAfter()) {
                    visitInsn(ACONST_NULL)
                    val injectArgsLen = Type.getArgumentTypes(
                        injectMethod.getMethodDesc()
                    ).size - 1
                    val originArgsLen = Type.getArgumentTypes(mDesc).size
                    if (injectArgsLen != 0) {
                        loadArgs(originArgsLen - injectArgsLen, injectArgsLen)
                    }
                    invokeStatic(
                        Type.getObjectType(injectMethod.getClassName()), Method(
                            injectMethod.getMethodName(), injectMethod.getMethodDesc()
                        )
                    )
                }
            }
            mContext.markModified()
        }

        override fun onMethodExit(opcode: Int) {
            val targetMethod: TargetMethod = findTargetMethod(mName, mDesc) ?: return
            for (injectMethod in targetMethod.getInjectMethods()) {
                if (injectMethod!!.isAfter()) {
                    visitInsn(ACONST_NULL)
                    val injectArgsLen = Type.getArgumentTypes(
                        injectMethod.getMethodDesc()
                    ).size - 1
                    val originArgsLen = Type.getArgumentTypes(mDesc).size
                    if (injectArgsLen != 0) {
                        loadArgs(originArgsLen - injectArgsLen, injectArgsLen)
                    }
                    invokeStatic(
                        Type.getObjectType(injectMethod.getClassName()), Method(
                            injectMethod.getMethodName(), injectMethod.getMethodDesc()
                        )
                    )
                }
            }
            mNeedInjectTargetMethods.remove(targetMethod)
            mContext.markModified()
        }
    }
}