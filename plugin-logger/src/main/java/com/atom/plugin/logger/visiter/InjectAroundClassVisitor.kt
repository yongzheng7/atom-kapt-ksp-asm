package com.atom.plugin.logger.visiter


import com.atom.plugin.core.Log
import com.atom.plugin.logger.hook.HookClassesConfig.getAroundHookClasses
import com.atom.plugin.logger.hook.TargetClass
import com.atom.plugin.logger.hook.TargetMethod
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method


class InjectAroundClassVisitor : ClassVisitor {

    private val mContext: Context
    private val mLog: Log
    private var mCurrentClass: String? = null

    constructor(api: Int, classVisitor: ClassVisitor, context: Context): super(api, classVisitor) {
        mContext = context
        mLog = mContext.getLog()
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<String?>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        mCurrentClass = name
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        val mv = super.visitMethod(access, name, desc, signature, exceptions)
        return AroundMethodVisitor(api, mv, access, name, desc)
    }

    private inner class AroundMethodVisitor internal constructor(
        api: Int,
        mv: MethodVisitor?,
        access: Int,
        name: String?,
        desc: String?
    ) :
        GeneratorAdapter(api, mv, access, name, desc) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            desc: String,
            itf: Boolean
        ) {
            val targetMethod = findTargetMethod(owner, name, desc)
            if (targetMethod != null) {
                val originalMethod = Method(name, desc)
                var callObject = -1
                val locals = IntArray(originalMethod.argumentTypes.size)
                for (i in locals.indices.reversed()) {
                    locals[i] = newLocal(originalMethod.argumentTypes[i])
                    storeLocal(locals[i])
                }
                if (opcode != Opcodes.INVOKESTATIC) {
                    callObject = newLocal(Type.getObjectType(owner))
                    storeLocal(callObject)
                }
                for (injectMethod in targetMethod.getInjectMethods()) {
                    if (!injectMethod.isAfter()) {
                        if (callObject >= 0) {
                            loadLocal(callObject)
                        }
                        for (tmpLocal in locals) {
                            loadLocal(tmpLocal)
                        }
                        invokeStatic(
                            Type.getObjectType(injectMethod.getClassName()),
                            Method(injectMethod.getMethodName(), injectMethod.getMethodDesc())
                        )
                        mLog.w(mCurrentClass + ": " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===Before===> " + owner + "#" + name + desc)
                    }
                }
                if (callObject >= 0) {
                    loadLocal(callObject)
                }
                for (tmpLocal in locals) {
                    loadLocal(tmpLocal)
                }
                super.visitMethodInsn(opcode, owner, name, desc, itf)
                for (injectMethod in targetMethod.getInjectMethods()) {
                    if (injectMethod.isAfter()) {
                        if (callObject >= 0) {
                            loadLocal(callObject)
                        }
                        for (tmpLocal in locals) {
                            loadLocal(tmpLocal)
                        }
                        invokeStatic(
                            Type.getObjectType(injectMethod.getClassName()),
                            Method(injectMethod.getMethodName(), injectMethod.getMethodDesc())
                        )
                        mLog.w(mCurrentClass + ": " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===After===> " + owner + "#" + name + desc)
                    }
                }
                mContext.markModified()
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf)
            }
        }
    }

    private fun findTargetMethod(
        className: String,
        methodName: String,
        methodDesc: String
    ): TargetMethod? {
        return findTargetClass(className)?.getTargetMethod(methodName, methodDesc)
    }

    private fun findTargetClass(className: String): TargetClass? {
        val aroundHookClasses: Map<String, TargetClass> = getAroundHookClasses()
        for (clazz in aroundHookClasses.keys) {
            if (isAssignable(className, clazz)) {
                return aroundHookClasses[clazz]
            }
        }
        return null
    }

    private fun isAssignable(subClassNameArgs: String, superClassNameArgs: String): Boolean {
        var subClassName = subClassNameArgs
        var superClassName = superClassNameArgs
        if (subClassName.contains("/")) {
            subClassName = subClassName.replace("/", ".")
        }
        if (superClassName.contains("/")) {
            superClassName = superClassName.replace("/", ".")
        }
        try {
            val subClass = mContext.getClassLoader().loadClass(subClassName)
            val superClass = mContext.getClassLoader().loadClass(superClassName)
            return superClass.isAssignableFrom(subClass)
        } catch (ignored: ClassNotFoundException) {
        } catch (ignored: NoClassDefFoundError) {
        } catch (ignored: SecurityException) {
        }
        return false
    }
}