package com.atom.module.core.aap

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.atom.module.annotation.aap.AapImpl
import com.atom.module.annotation.aap.AapImplEntry
import com.atom.module.api.aap.AapContext
import com.atom.module.api.aap.AapFilter
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier

object AapEngine : AapContext {

    private val registerClass: MutableSet<KClass<out AapImplEntry>> = HashSet<KClass<out AapImplEntry>>()

    private const val META_DATA_NAME = "com.atom.apt.proxy"

    private fun loadProxyClass() {}

    private fun loadProxyClassByManifest(application: Application) {
        val appInfo: ApplicationInfo = try {
            application.packageManager.getApplicationInfo(
                application.packageName,
                PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }
        for (key in appInfo.metaData.keySet()) {
            if (!key.startsWith(META_DATA_NAME)) {
                continue
            }
            registerClass(appInfo.metaData.getString(key, null))
        }
    }

    private fun registerClass(className: String) {
        Log.e("register class ", Objects.requireNonNull(className))
        if (!TextUtils.isEmpty(className)) {
            try {
                val clazz = Class.forName(className).kotlin
                if (clazz.isInstance(AapImplEntry::class)) {
                    registerClass.add(clazz as KClass<out AapImplEntry>)
                }
            } catch (e: Exception) {
                Log.e("register class error:$className", Objects.requireNonNull(e.localizedMessage))
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