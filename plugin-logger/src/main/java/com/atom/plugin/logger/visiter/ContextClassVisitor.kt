package com.atom.plugin.logger.visiter


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

class ContextClassVisitor : ClassVisitor {

    private val mContext: Context

    constructor(api: Int, context: Context) : super(api) {
        mContext = context
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        sig: String?,
        superName: String?,
        interfaces: Array<String?>?
    ) {
        mContext.setClassName(name)
        mContext.setSuperClassName(superName)
        mContext.setAbstract(access and Opcodes.ACC_ABSTRACT != 0)
        super.visit(version, access, name, sig, superName, interfaces)
    }

}