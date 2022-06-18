package com.atom.plugin.logger.visiter


import com.atom.plugin.core.Log
import com.atom.plugin.logger.hook.HookClassesConfig.getSuperHookClasses
import com.atom.plugin.logger.hook.TargetClass
import com.atom.plugin.logger.hook.TargetMethod
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ACONST_NULL
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method


class DesugaringClassVisitor : ClassVisitor {

    private val TAG = "DesugaringClassVisitor"

    private val mContext: Context
    private val mLog: Log

    private val mNeedInjectTargetMethods: MutableSet<TargetMethod> = HashSet()
    private val mGenerateMethodBlocks = HashMap<String, GenerateMethodBlock>()
    private var mGenerateMethodIndex = 0

    constructor(api: Int, cv: ClassVisitor?, context: Context) : super(api, cv) {
        mContext = context
        mLog = context.getLog()
    }

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
        return DesugaringMethodVisitor(api, methodVisitor, access, name, desc)
    }

    override fun visitEnd() {
        if (mGenerateMethodBlocks.isEmpty()) {
            super.visitEnd()
            return
        }
        for (methodBlock in mGenerateMethodBlocks.values) {
            generateMethod(methodBlock)
        }
        super.visitEnd()
    }

    fun getNeedInjectTargetMethods(): MutableSet<TargetMethod> {
        return mNeedInjectTargetMethods
    }

    private fun generateMethod(methodBlock: GenerateMethodBlock) {
        mLog.w(TAG + ": generateMethod: " + methodBlock.mMethodName + "#" + methodBlock.mMethodDesc)
        val access = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC
        val visitor =
            super.visitMethod(access, methodBlock.mMethodName, methodBlock.mMethodDesc, null, null)
        val adapter =
            GeneratorAdapter(visitor, access, methodBlock.mMethodName, methodBlock.mMethodDesc)
        adapter.visitCode()
        val arguments = Type.getArgumentTypes(methodBlock.mMethodDesc)
        val isStaticOrigin = methodBlock.mOriginHandle.tag == Opcodes.H_INVOKESTATIC
        for (injectMethod in methodBlock.mTargetMethod.getInjectMethods()) {
            if (!injectMethod!!.isAfter()) {
                adapter.visitInsn(ACONST_NULL)
                if (isStaticOrigin) {
                    adapter.loadArgs()
                } else {
                    if (arguments.size > 1) {
                        adapter.loadArgs(1, arguments.size - 1)
                    }
                }
                adapter.invokeStatic(
                    Type.getObjectType(injectMethod.getClassName()), Method(
                        injectMethod.getMethodName(), injectMethod.getMethodDesc()
                    )
                )
            }
        }
        adapter.loadArgs()
        val owner = Type.getObjectType(methodBlock.mOriginHandle.owner)
        val method = Method(methodBlock.mOriginHandle.name, methodBlock.mOriginHandle.desc)
        when (methodBlock.mOriginHandle.tag) {
            Opcodes.H_INVOKEINTERFACE -> adapter.invokeInterface(owner, method)
            Opcodes.H_INVOKESPECIAL -> throw RuntimeException("should not has invoke special: " + methodBlock.mMethodName + "#" + methodBlock.mMethodDesc)
            Opcodes.H_INVOKESTATIC -> adapter.invokeStatic(owner, method)
            Opcodes.H_INVOKEVIRTUAL -> adapter.invokeVirtual(owner, method)
            else -> {}
        }
        for (injectMethod in methodBlock.mTargetMethod.getInjectMethods()) {
            if (injectMethod!!.isAfter()) {
                adapter.visitInsn(ACONST_NULL)
                if (isStaticOrigin) {
                    adapter.loadArgs()
                } else {
                    if (arguments.size > 1) {
                        adapter.loadArgs(1, arguments.size - 1)
                    }
                }
                adapter.invokeStatic(
                    Type.getObjectType(injectMethod.getClassName()), Method(
                        injectMethod.getMethodName(), injectMethod.getMethodDesc()
                    )
                )
            }
        }
        adapter.returnValue()
        adapter.visitMaxs(arguments.size, arguments.size)
        adapter.visitEnd()
    }

    private inner class DesugaringMethodVisitor(
        api: Int,
        mv: MethodVisitor,
        access: Int,
        private val mName: String,
        desc: String
    ) :
        AdviceAdapter(api, mv, access, mName, desc) {
        override fun visitInvokeDynamicInsn(
            lambdaMethodName: String,
            desc: String,
            bsm: Handle,
            vararg bsmArgs: Any
        ) {
            mLog.w(
                String.format(
                    "DesugaringMethodVisitor(%s): on method %s",
                    mContext.getClassName(),
                    mName
                )
            )
            val index = desc.lastIndexOf(")L")
            if (index == -1) {
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, *bsmArgs)
                return
            }
            val interfaceClazzName = desc.substring(index + 2, desc.length - 1)
            val targetClass: TargetClass? = getSuperHookClasses()?.get(interfaceClazzName)
            if (targetClass == null) {
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, *bsmArgs)
                return
            }
            val lambdaMethodDesc = (bsmArgs[0] as Type).descriptor
            val targetMethod: TargetMethod? =
                targetClass.getTargetMethod(lambdaMethodName, lambdaMethodDesc)
            if (targetMethod == null) {
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, *bsmArgs)
                return
            }
            val handle = bsmArgs[1] as Handle
            if (lambdaMethodName == handle.name) {
                // 校验实现方法是不是实现了对应接口的实现方法， 如果是则过滤，交给 InjectSuperClassVisitor 进行处理
                if (mContext.isAssignable(handle.owner, interfaceClazzName)) {
                    mLog.w(
                        String.format(
                            "DesugaringClassVisitor(%s): skipped on method %s",
                            mContext.getClassName(),
                            mName
                        )
                    )
                    super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, *bsmArgs)
                    return
                }
            }
            if (handle.owner == mContext.getClassName()) {
                // 实现方法在此类中
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, *bsmArgs)
                val needInjectMethod = TargetMethod(handle.name, handle.desc)
                needInjectMethod.addInjectMethods(targetMethod.getInjectMethods())
                mNeedInjectTargetMethods.add(needInjectMethod)
            } else {
                val key = interfaceClazzName + handle.owner + handle.name + handle.desc
                var methodBlock: GenerateMethodBlock? = mGenerateMethodBlocks.get(key)
                if (methodBlock == null) {
                    val methodDesc: String
                    methodDesc = if (handle.tag == H_INVOKESTATIC) {
                        "(" + handle.desc.replace("(", "")
                    } else {
                        "(L" + handle.owner + ";" + handle.desc.replace("(", "")
                    }
                    methodBlock = GenerateMethodBlock(
                        "lambda\$GIO$$mGenerateMethodIndex",
                        methodDesc,
                        targetMethod,
                        handle
                    )
                    mGenerateMethodBlocks.put(key, methodBlock)
                    mGenerateMethodIndex++
                }

                val newBsmArgs = mutableListOf<Any>()
                bsmArgs.forEachIndexed { index, any ->
                    if (index == 0) {
                        newBsmArgs.add(
                            Handle(
                                H_INVOKESTATIC,
                                mContext.getClassName(),
                                methodBlock.mMethodName,
                                methodBlock.mMethodDesc,
                                false
                            )
                        )
                    } else {
                        newBsmArgs.add(any)
                    }
                }
                // Check for exact match on non-receiver captured arguments
                // (实例方法中this是receiver, 这里进行更改， 防止改为invoke_static 后Lambda Runtime校验失败 )
                val newDesc: String = if (handle.tag == H_INVOKESTATIC) {
                    desc
                } else {
                    newDesc(desc, handle.owner)
                }
                mContext.markModified()
                super.visitInvokeDynamicInsn(lambdaMethodName, newDesc, bsm, *(newBsmArgs.toTypedArray()))
            }
        }

        private fun newDesc(oldDesc: String, realOwner: String): String {
            val firstSemiColon = oldDesc.indexOf(';')
            return "(L" + realOwner + oldDesc.substring(firstSemiColon)
        }
    }

    private class GenerateMethodBlock internal constructor(
        val mMethodName: String,
        val mMethodDesc: String,
        val mTargetMethod: TargetMethod,
        val mOriginHandle: Handle
    )
}