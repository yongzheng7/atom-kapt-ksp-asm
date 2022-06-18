package com.atom.plugin.logger.hook

import java.util.*

class TargetClass(name: String) {
    private val mName: String = name
    private val mTargetMethods: MutableSet<TargetMethod> = HashSet()
    private var mInterface = false

    fun addTargetMethod(method: TargetMethod) {
        mTargetMethods.add(method)
    }

    fun getName(): String {
        return mName
    }

    fun getTargetMethods(): kotlin.collections.Set<TargetMethod> {
        return Collections.unmodifiableSet(mTargetMethods)
    }

    fun getTargetMethod(name: String, desc: String): TargetMethod? {
        for (method in mTargetMethods) {
            if (name == method.getName() && desc == method.getDesc()) {
                return method
            }
        }
        return null
    }

    fun setInterface(anInterface: Boolean) {
        mInterface = anInterface
    }

    fun isInterface(): Boolean {
        return mInterface
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as TargetClass
        return mName == that.mName
    }

    override fun hashCode(): Int {
        return mName.hashCode()
    }
}