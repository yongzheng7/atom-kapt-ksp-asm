package com.atom.module.aap.annotation

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AapImplContext
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/1/19
 * @Copyright: 2022/1/19 www.rongdasoft.com Inc. All rights reserved.
 */
abstract class AapImplEntry {

    private val mAapImplMap = hashMapOf<Class<*>, AapImplHashMap<*>>()

    protected fun <T> add(
        name: String,
        apiClass: Class<T>,
        implClass: Class<out T>,
        version: Long
    ) {
        val aapImplMap: AapImplHashMap<*> = synchronized(mAapImplMap) {
            mAapImplMap[apiClass] ?: let {
                val temp = AapImplHashMap<T>()
                mAapImplMap[apiClass] = temp
                temp
            }
        }
        (aapImplMap as AapImplHashMap<T>)[implClass] = AapImplVersion(name, version)
    }

    fun <T> getAapImplMap(apiClass: Class<T>): Map<Class<out T>, AapImplVersion> {
        val synchronized = synchronized(mAapImplMap) {
            mAapImplMap.get(apiClass) ?: let {
                val temp = AapImplHashMap<T>()
                mAapImplMap[apiClass] = temp
                temp
            }
        }
        return synchronized as AapImplHashMap<T>
    }
}