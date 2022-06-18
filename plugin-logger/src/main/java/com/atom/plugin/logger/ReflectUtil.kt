package com.atom.plugin.logger

import java.lang.reflect.Field;
import java.lang.reflect.Method;


object ReflectUtil {

    fun getMethod(clazz: Class<*>, methodName: String, vararg params: Class<*>?): Method? {
        return try {
            clazz.getMethod(methodName, *params)
        } catch (e: NoSuchMethodException) {
            null
        }
    }

    fun findFieldObj(currentArgs: Class<*>, fieldName: String): Field? {
        var current = currentArgs
        while (current != Any::class.java) {
            current = try {
                val field: Field = current.getDeclaredField(fieldName)
                field.isAccessible = true
                return field
            } catch (e: NoSuchFieldException) {
                current.superclass
            }
        }
        return null
    }

    fun <T> findField(instance: Any, fieldName: String): T? {
        val field: Field? = findFieldObj(instance.javaClass, fieldName)
        if (field != null) {
            try {
                return field.get(instance) as T?
            } catch (ignored: IllegalAccessException) {
            }
        }
        return null
    }
}