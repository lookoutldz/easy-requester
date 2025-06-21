package io.github.lookoutldz.easyrequester.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

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
    return try {
        // 使用Jackson的公共API获取已注册的模块ID
        val registeredModuleIds = objectMapper.registeredModuleIds
        registeredModuleIds.any { moduleId ->
            if (moduleId is String) {
                moduleId.contains("KotlinModule")
            } else {
                false
            }
        }
    } catch (e: Exception) {
        false
    }
}

/**
 * 深度检测类型及其所有属性中是否存在 Kotlin data class
 * 支持递归检测嵌套属性和泛型类型
 * 
 * @param type 要检测的类型
 * @param visitedTypes 已访问的类型集合，用于防止循环引用
 * @param maxDepth 最大递归深度，防止无限递归
 * @param currentDepth 当前递归深度
 * @return 如果类型或其任何属性包含 Kotlin data class 则返回 true
 */
/**
 * 改进的深度检测方法
 */
internal fun containsDataClassDeep(
    type: Type,
    visitedTypes: MutableSet<String> = mutableSetOf(), // 使用字符串避免Type对象内存问题
    maxDepth: Int = 10,
    currentDepth: Int = 0
): Boolean {

    // 防止无限递归
    val typeKey = type.toString()
    if (currentDepth >= maxDepth || typeKey in visitedTypes) {
        return false
    }

    visitedTypes.add(typeKey)
    
    return try {
        when (type) {
            is Class<*> -> {
                // 检查当前类型是否是 data class
                if (isDataClass(type)) {
                    true
                } else {
                    // 递归检查类的所有属性
                    checkClassPropertiesDeep(type, visitedTypes, maxDepth, currentDepth + 1)
                }
            }
            
            is ParameterizedType -> {
                // 检查原始类型
                val rawType = type.rawType
                if (rawType is Class<*> && isDataClass(rawType)) {
                    true
                } else {
                    // 检查泛型参数
                    type.actualTypeArguments.any { typeArg ->
                        containsDataClassDeep(typeArg, visitedTypes, maxDepth, currentDepth + 1)
                    } || (rawType is Class<*> && checkClassPropertiesDeep(rawType, visitedTypes, maxDepth, currentDepth + 1))
                }
            }
            
            is WildcardType -> {
                // 检查通配符类型的边界
                type.upperBounds.any { upperBound ->
                    containsDataClassDeep(upperBound, visitedTypes, maxDepth, currentDepth + 1)
                } || type.lowerBounds.any { lowerBound ->
                    containsDataClassDeep(lowerBound, visitedTypes, maxDepth, currentDepth + 1)
                }
            }
            
            else -> false
        }
    } catch (e: Exception) {
        // 记录异常但不抛出，继续处理
        false
    } finally {
        visitedTypes.remove(typeKey)
    }
}

/**
 * 检查类的所有属性是否包含 data class
 */
private fun checkClassPropertiesDeep(
    clazz: Class<*>,
    visitedTypes: MutableSet<String>,
    maxDepth: Int,
    currentDepth: Int
): Boolean {
    try {
        // 改进的包名检查
        if (clazz.isPrimitive || isSystemClass(clazz)) {
            return false
        }
        
        // 使用 Kotlin 反射检查属性
        val kClass = clazz.kotlin
        for (property in kClass.memberProperties) {
            try {
                val propertyType = (property as? KProperty1<*, *>)?.returnType?.javaType
                if (propertyType != null && 
                    containsDataClassDeep(propertyType, visitedTypes, maxDepth, currentDepth)) {
                    return true
                }
            } catch (e: Exception) {
                // 忽略单个属性的异常，继续检查其他属性
                continue
            }
        }
        
        // 如果 Kotlin 反射失败，尝试使用 Java 反射
        for (field in clazz.declaredFields) {
            try {
                if (containsDataClassDeep(field.genericType, visitedTypes, maxDepth, currentDepth)) {
                    return true
                }
            } catch (e: Exception) {
                continue
            }
        }
    } catch (e: Exception) {
        // 忽略类级别的异常
    }
    
    return false
}

/**
 * 检查是否为系统类
 */
private fun isSystemClass(clazz: Class<*>): Boolean {
    val packageName = clazz.packageName
    return packageName?.let { pkg ->
        pkg.startsWith("java.") || 
        pkg.startsWith("kotlin.") || 
        pkg.startsWith("javax.") ||
        pkg.startsWith("sun.") ||
        pkg.startsWith("com.sun.")
    } ?: false
}

/**
 * 检查 TypeReference 及其深层属性中是否包含 Kotlin data class
 */
internal fun <T> dataClassInTypeReference(typeRef: TypeReference<T>?): Boolean {
    if (typeRef == null) return false
    
    try {
        val typeRefClass = typeRef.javaClass
        val superClass = typeRefClass.genericSuperclass
        
        if (superClass is ParameterizedType) {
            val actualTypeArgument = superClass.actualTypeArguments[0]
            return containsDataClassDeep(actualTypeArgument)
        }
    } catch (e: Exception) {
        // 忽略异常
    }
    
    return false
}

/**
 * 检查 Class 及其深层属性中是否包含 Kotlin data class
 */
internal fun dataClassInClass(clazz: Class<*>?): Boolean {
    if (clazz == null) return false
    return containsDataClassDeep(clazz as Type)
}

/**
 * 检查 KClass 及其深层属性中是否包含 Kotlin data class
 */
internal fun dataClassInKClass(kClass: KClass<*>?): Boolean {
    if (kClass == null) return false
    return containsDataClassDeep(kClass.java)
}

/**
 * 共享线程安全的 ObjectMapper
 * 使用lazy委托确保线程安全的初始化
 */
internal val defaultObjectMapper by lazy { ObjectMapper() }
internal val kotlinObjectMapper by lazy {
    ObjectMapper()
        // Kotlin支持
        .registerKotlinModule()
        // Java时间模块支持
        .registerModule(JavaTimeModule())
        // 日期序列化格式
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // 添加以下配置以提高稳定性，忽略JSON中存在但Java对象中不存在的属性，避免因未知属性导致的反序列化失败
        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // 允许非引号字段名，增加解析灵活性
        .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        // 允许单引号，增加解析灵活性
        .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
}

internal fun getEffectiveObjectMapper(isKotlinData: Boolean): ObjectMapper {
    return if (isKotlinData) {
        kotlinObjectMapper
    } else {
        defaultObjectMapper
    }
}