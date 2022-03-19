package com.atom.plugin.core.test.aap

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: SuperScanSet
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/15
 * @Copyright: 2022/2/15 www.rongdasoft.com Inc. All rights reserved.
 */
sealed class ScanSet(val name: String) {
    /**
     * 扫描结果 {@link #interfaceName}
     * @return 返回类名的集合   实现该指定名称的子类
     */
    val classList = arrayListOf<String>()

    abstract fun isSuper(): Boolean

    fun isInterface() = !isSuper()
}

class SuperScanSet(name: String) : ScanSet(name) {
    override fun isSuper(): Boolean {
        return true
    }
}

class InterfaceScanSet(name: String) : ScanSet(name) {
    override fun isSuper(): Boolean {
        return false
    }
}