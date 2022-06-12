package com.atom.module.core.aap

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.atom.module.annotation.aap.AapImplEntry
import com.atom.module.api.aap.AapContext
import com.atom.module.api.aap.AapFilter
import java.util.*
import kotlin.reflect.KClass

object AapEngine : AapContext {

    init {
        Log.e(
            "MainActivity",
            "AapEngine init >>>>>>>>>>>>>>>>>>>>>>"
        )
        loadProxyClass()
        Log.e("MainActivity", "AapEngine init <<<<<<<<<<<<<<<<<<<<<<")
    }

    private val registerClass: MutableSet<KClass<out AapImplEntry>> =
        HashSet<KClass<out AapImplEntry>>()

    private fun loadProxyClass() {
    }

    override fun toString(): String {
        return "registerClass  $registerClass"
    }

    private fun registerClass(className: String) {
        Log.e("MainActivity", "className >>> $className")
        if (!TextUtils.isEmpty(className)) {
            try {
                val clazz = Class.forName(className).kotlin
                if (clazz.isInstance(AapImplEntry::class)) {
                    registerClass.add(clazz as KClass<out AapImplEntry>)
                }
            } catch (e: Exception) {
                Log.e(
                    "MainActivity",
                    " ${className} , error ${Objects.requireNonNull(e.localizedMessage)}"
                )
            }
        }
    }

    //**************************************

    override fun <T> getApis(requiredType: Class<T>): Collection<Class<out T>> {
        TODO("Not yet implemented")
    }

    override fun <T> getApis(
        requiredType: Class<T>,
        name: String,
        useRegex: Boolean
    ): Collection<Class<out T>> {
        TODO("Not yet implemented")
    }

    override fun <T> getApis(
        requiredType: Class<T>,
        filter: AapFilter<T>
    ): Collection<Class<out T>> {
        TODO("Not yet implemented")
    }

    override fun <T> getApi(requiredType: Class<T>): Class<out T>? {
        TODO("Not yet implemented")
    }

    override fun <T> getApi(requiredType: Class<T>, version: Long): Class<out T>? {
        TODO("Not yet implemented")
    }

    override fun <T> getApi(
        requiredType: Class<T>,
        name: String,
        version: Long,
        useRegex: Boolean
    ): Class<out T>? {
        TODO("Not yet implemented")
    }

    override fun <T> setImpl(requiredType: Class<T>, name: String, version: Long, entity: T) {
        TODO("Not yet implemented")
    }

    override fun <T> getImpl(requiredType: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T> getImpl(requiredType: Class<T>, name: String): T {
        TODO("Not yet implemented")
    }

    override fun <T> getImpl(requiredType: Class<T>, version: Long): T {
        TODO("Not yet implemented")
    }

    override fun <T> getImpl(
        requiredType: Class<T>,
        name: String,
        version: Long,
        useRegex: Boolean
    ): T {
        TODO("Not yet implemented")
    }

    override fun <T> hasApi(requiredType: Class<T>, name: String, version: Long): T {
        TODO("Not yet implemented")
    }

    override fun <T> hasApi(requiredType: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T> newApi(api: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T> newApi(api: Class<T>, name: String): T {
        TODO("Not yet implemented")
    }

    override fun <T> newApi(api: Class<T>, name: String, version: Long): T {
        TODO("Not yet implemented")
    }

    override fun cachePut(data: Any): String {
        TODO("Not yet implemented")
    }

    override fun cacheGet(key: String): Any? {
        TODO("Not yet implemented")
    }

    override fun cacheRemove(key: String): Any? {
        TODO("Not yet implemented")
    }

    override fun setImplEnabled(name: String, enable: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getImplEnabled(name: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAppContext(): Context {
        TODO("Not yet implemented")
    }

}