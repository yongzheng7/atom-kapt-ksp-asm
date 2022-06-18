package com.atom.plugin.logger.hook

class InjectMethod(className: String, methodName: String, methodDesc: String, isAfter: Boolean) {
    private val mClassName: String = className
    private val mMethodName: String = methodName
    private val mMethodDesc: String = methodDesc
    private var mIsAfter = isAfter

    fun getClassName(): String {
        return mClassName
    }

    fun getMethodName(): String {
        return mMethodName
    }

    fun getMethodDesc(): String {
        return mMethodDesc
    }

    fun isAfter(): Boolean {
        return mIsAfter
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as InjectMethod
        if (mIsAfter != that.mIsAfter) return false
        if (mClassName != that.mClassName) return false
        return if (mMethodName != that.mMethodName) false else mMethodDesc == that.mMethodDesc
    }

    override fun hashCode(): Int {
        var result = mClassName.hashCode()
        result = 31 * result + mMethodName.hashCode()
        result = 31 * result + mMethodDesc.hashCode()
        result = 31 * result + if (mIsAfter) 1 else 0
        return result
    }
}