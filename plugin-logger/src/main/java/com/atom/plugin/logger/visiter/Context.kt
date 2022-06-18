package com.atom.plugin.logger.visiter

import com.atom.plugin.core.Log

class Context {

    private val mLog: Log
    private val mClassLoader: ClassLoader

    private var mClassName: String? = null
    private var mSuperClassName: String? = null
    private var mClassModified = false
    private var mIsAbstract = false

    constructor(log: Log, classLoader: ClassLoader) {
        mLog = log
        mClassLoader = classLoader
    }

    fun getClassLoader(): ClassLoader {
        return mClassLoader
    }

    fun getLog(): Log {
        return mLog
    }

    fun getClassName(): String? {
        return mClassName
    }

    fun setClassName(className: String?) {
        mClassName = className
    }

    fun getSuperClassName(): String? {
        return mSuperClassName
    }

    fun setSuperClassName(superClassName: String?) {
        mSuperClassName = superClassName
    }

    fun isClassModified(): Boolean {
        return mClassModified
    }

    fun markModified() {
        mClassModified = true
    }

    fun isAbstract(): Boolean {
        return mIsAbstract
    }

    fun setAbstract(anAbstract: Boolean) {
        mIsAbstract = anAbstract
    }

    fun isAssignable(subClassNameArgs: String, superClassNameArgs: String): Boolean {
        var subClassName = subClassNameArgs
        var superClassName = superClassNameArgs
        if (subClassName.contains("/")) {
            subClassName = subClassName.replace("/", ".")
        }
        if (superClassName.contains("/")) {
            superClassName = superClassName.replace("/", ".")
        }
        try {
            val subClass = getClassLoader().loadClass(subClassName)
            val superClass = getClassLoader().loadClass(superClassName)
            return superClass.isAssignableFrom(subClass)
        } catch (ignored: ClassNotFoundException) {
        }
        return false
    }
}