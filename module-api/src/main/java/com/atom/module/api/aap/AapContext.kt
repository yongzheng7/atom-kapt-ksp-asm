package com.atom.module.api.aap

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.StringRes
import java.util.concurrent.Future

interface AapContext {

    fun <T> getApis(
        requiredType: Class<T>
    ): Collection<Class<out T>>

    fun <T> getApis(
        requiredType: Class<T>,
        name: String,
        useRegex: Boolean
    ): Collection<Class<out T>>

    fun <T> getApis(
        requiredType: Class<T>, filter: AapFilter<T>
    ): Collection<Class<out T>>

    fun <T> getApi(
        requiredType: Class<T>
    ): Class<out T>?

    fun <T> getApi(
        requiredType: Class<T>, version: Long
    ): Class<out T>?

    fun <T> getApi(
        requiredType: Class<T>,
        name: String,
        version: Long,
        useRegex: Boolean
    ): Class<out T>?

    fun <T> setImpl(
        requiredType: Class<T>,
        name: String,
        version: Long,
        entity: T
    )

    fun <T> getImpl(
        requiredType: Class<T>
    ): T

    fun <T> getImpl(
        requiredType: Class<T>,
        name: String
    ): T

    fun <T> getImpl(
        requiredType: Class<T>,
        version: Long
    ): T

    fun <T> getImpl(
        requiredType: Class<T>,
        name: String,
        version: Long,
        useRegex: Boolean
    ): T

    fun <T> hasApi(
        requiredType: Class<T>,
        name: String,
        version: Long
    ): T

    fun <T> hasApi(requiredType: Class<T>): T

    fun <T> newApi(api: Class<T>): T

    fun <T> newApi(api: Class<T>, name: String): T

    fun <T> newApi(api: Class<T>, name: String, version: Long): T

    fun cachePut(data: Any): String

    fun cacheGet(key: String): Any?

    fun cacheRemove(key: String): Any?

    fun setImplEnabled(name: String, enable: Boolean)

    fun getImplEnabled(name: String): Boolean

    fun getAppContext(): Context
}