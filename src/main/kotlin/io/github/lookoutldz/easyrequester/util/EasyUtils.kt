package io.github.lookoutldz.easyrequester.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

/**
 * 检查 TypeReference 中的泛型是否是 Kotlin 的 data class
 */
internal fun <T> isDataClass(typeRef: TypeReference<T>?): Boolean {
    try {
        if (typeRef == null) {
            return false
        }

        // 获取TypeReference的Class
        val typeRefClass = typeRef.javaClass

        // 获取泛型超类
        val superClass = typeRefClass.genericSuperclass

        if (superClass is java.lang.reflect.ParameterizedType) {
            // 获取实际类型参数
            val actualTypeArgument = superClass.actualTypeArguments[0]

            if (actualTypeArgument is Class<*>) {
                // 转换为Kotlin的KClass并检查
                return actualTypeArgument.kotlin.isData
            }
        }
        return false
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

/**
 * 检查 Java Class 是否是 Kotlin 的 data class
 */
internal fun isDataClass(clazz: Class<*>?): Boolean {
    return try {
        if (clazz == null) {
            return false
        }
        // 将Java Class转换为Kotlin KClass
        val kClass = clazz.kotlin
        // 检查是否是data class
        kClass.isData
    } catch (e: Exception) {
        false
    }
}

/**
 * 检查 Kotlin Class 是否是 data class
 */
internal fun isDataClass(dataClass: KClass<*>?): Boolean {
    return dataClass?.isData == true
}

internal fun isKotlinModuleRegistered(objectMapper: ObjectMapper): Boolean {
    try {
        // 获取已注册模块的字段
        val registeredModulesField = ObjectMapper::class.java.getDeclaredField("_registeredModuleTypes")
        registeredModulesField.isAccessible = true

        // 获取已注册模块的集合，增加空值检查
        val registeredModules = registeredModulesField.get(objectMapper)
        if (registeredModules == null) {
            return false // 如果为null，说明没有注册任何模块
        }

        if (registeredModules is Set<*>) {
            // 检查是否包含KotlinModule
            return registeredModules.any {
                it.toString().contains("KotlinModule")
            }
        }
        return false
    } catch (e: Exception) {
        // 捕获所有可能的异常，包括NoSuchFieldException
        return false
    }
}
