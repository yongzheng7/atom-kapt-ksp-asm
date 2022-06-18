package com.atom.plugin.logger.hook
import java.util.*

class TargetMethod(name: String, desc: String) {

    private val mName: String = name
    private val mDesc: String = desc
    private val mInjectMethods: MutableSet<InjectMethod> = HashSet<InjectMethod>()

    fun getName(): String {
        return mName
    }

    fun getDesc(): String {
        return mDesc
    }

    fun addInjectMethod(method: InjectMethod) {
        mInjectMethods.add(method)
    }

    fun addInjectMethods(methods: Set<InjectMethod>) {
        mInjectMethods.addAll(methods)
    }

    fun getInjectMethods(): Set<InjectMethod> {
        return Collections.unmodifiableSet(mInjectMethods)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as TargetMethod
        return if (mName != that.mName) false else mDesc == that.mDesc
    }

    override fun hashCode(): Int {
        var result = mName.hashCode()
        result = 31 * result + mDesc.hashCode()
        return result
    }

}