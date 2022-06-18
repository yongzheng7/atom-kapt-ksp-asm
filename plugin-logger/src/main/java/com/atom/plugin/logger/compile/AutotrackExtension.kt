package com.atom.plugin.logger.compile

class AutotrackExtension {
    private var mIsLogEnabled = false
    private var mIsDevelopment = true
    private var mExcludeOfficialPackages = true
    private var mExcludePackages: Array<String> ?= null

    fun isLogEnabled(): Boolean {
        return mIsLogEnabled
    }

    fun setLogEnabled(logEnabled: Boolean) {
        mIsLogEnabled = logEnabled
    }

    fun isDevelopment(): Boolean {
        return mIsDevelopment
    }

    fun setDevelopment(development: Boolean) {
        mIsDevelopment = development
    }

    fun getExcludePackages(): Array<String>? {
        return mExcludePackages
    }

    fun setExcludePackages(excludePackages: Array<String>?) {
        mExcludePackages = excludePackages
    }

    fun isExcludeOfficialPackages(): Boolean {
        return mExcludeOfficialPackages
    }

    fun setExcludeOfficialPackages(mExcludeOfficialPackages: Boolean) {
        this.mExcludeOfficialPackages = mExcludeOfficialPackages
    }
}