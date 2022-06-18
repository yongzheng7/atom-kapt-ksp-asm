package com.atom.plugin.logger.visiter

import com.atom.plugin.core.Log
import com.atom.plugin.logger.hook.HookClassesConfig.getSuperHookClasses
import com.atom.plugin.logger.hook.InjectMethod
import com.atom.plugin.logger.hook.TargetClass
import com.atom.plugin.logger.hook.TargetMethod
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method


class InjectSuperClassVisitor :ClassVisitor {

    private val mContext: Context
    private val mLog: Log
    private val mTargetClasses: MutableList<TargetClass> = arrayListOf()
    private val mOverrideMethods: MutableSet<TargetMethod> = HashSet()
    private var mCurrentClass: String? = null

    constructor(api: Int, classVisitor: ClassVisitor?, context: Context):  super(api, classVisitor) {
        mContext = context
        mLog = context.getLog()
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<String?>
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        mCurrentClass = name
        val targetClass: TargetClass? = getSuperHookClasses()!![superName]
        if (targetClass != null) {
            mTargetClasses.add(targetClass)
        }
        for (i in interfaces) {
            val targetInterface: TargetClass? = getSuperHookClasses()!![i]
            if (targetInterface != null) {
                targetInterface.setInterface(true)
                mTargetClasses.add(targetInterface)
            }
        }
        if (mTargetClasses.isNotEmpty()) {
            mContext.markModified()
        }
    }

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        val mv = super.visitMethod(access, name, desc, signature, exceptions)
        for (targetClass in mTargetClasses) {
            val targetMethod: TargetMethod? = targetClass.getTargetMethod(name, desc)
            if (targetMethod != null) {
                mOverrideMethods.add(targetMethod)
                return InjectSuperMethodVisitor(
                    mv,
                    access,
                    name,
                    desc,
                    targetMethod.getInjectMethods()
                )
            }
        }
        return mv
    }

    override fun visitEnd() {
        for (targetClass in mTargetClasses) {
            for (targetMethod in targetClass.getTargetMethods()) {
                if (!mOverrideMethods.contains(targetMethod)) {
                    val injectMethods = targetMethod.getInjectMethods()
                    val m = Method(targetMethod.getName(), targetMethod.getDesc())
                    val mg = GeneratorAdapter(ACC_PUBLIC, m, null, null, cv)
                    for (injectMethod in injectMethods) {
                        if (!injectMethod.isAfter()) {
                            mg.loadThis()
                            mg.loadArgs()
                            mg.invokeStatic(
                                Type.getObjectType(injectMethod.getClassName()),
                                Method(injectMethod.getMethodName(), injectMethod.getMethodDesc())
                            )
                            mLog.w("Method Add: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperBefore===> " + mCurrentClass + "#" + targetMethod.getName() + targetMethod.getDesc())
                        }
                    }
                    if (!targetClass.isInterface()) {
                        mg.loadThis()
                        mg.loadArgs()
                        mg.invokeConstructor(
                            Type.getObjectType(targetClass.getName()),
                            Method(targetMethod.getName(), targetMethod.getDesc())
                        )
                    }
                    for (injectMethod in injectMethods) {
                        if (injectMethod.isAfter()) {
                            mg.loadThis()
                            mg.loadArgs()
                            mg.invokeStatic(
                                Type.getObjectType(injectMethod.getClassName()),
                                Method(injectMethod.getMethodName(), injectMethod.getMethodDesc())
                            )
                            mLog.w("Method Add: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperAfter===> " + mCurrentClass + "#" + targetMethod.getName() + targetMethod.getDesc())
                        }
                    }
                    mg.returnValue()
                    mg.endMethod()
                }
            }
        }
        super.visitEnd()
    }

    private inner class InjectSuperMethodVisitor(
        mv: MethodVisitor?,
        access: Int,
        private val mTargetMethodName: String,
        private val mTargetMethodDesc: String,
        private val mInjectMethods: Set<InjectMethod>
    ) :
        AdviceAdapter(
            ASM5, mv, access,
            mTargetMethodName,
            mTargetMethodDesc
        ) {
        override fun onMethodEnter() {
            super.onMethodEnter()
            for (injectMethod in mInjectMethods) {
                if (!injectMethod.isAfter()) {
                    loadThis()
                    loadArgs()
                    invokeStatic(
                        Type.getObjectType(injectMethod.getClassName()),
                        Method(injectMethod.getMethodName(), injectMethod.getMethodDesc())
                    )
                    mLog.w("Method Insert: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperBefore===> " + mCurrentClass + "#" + mTargetMethodName + mTargetMethodDesc)
                }
            }
        }

        override fun onMethodExit(opcode: Int) {
            for (injectMethod in mInjectMethods) {
                if (injectMethod.isAfter()) {
                    loadThis()
                    loadArgs()
                    invokeStatic(
                        Type.getObjectType(injectMethod.getClassName()),
                        Method(injectMethod.getMethodName(), injectMethod.getMethodDesc())
                    )
                }
                mLog.w("Method Insert: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperAfter===> " + mCurrentClass + "#" + mTargetMethodName + mTargetMethodDesc)
            }
            super.onMethodExit(opcode)
        }
    }

}