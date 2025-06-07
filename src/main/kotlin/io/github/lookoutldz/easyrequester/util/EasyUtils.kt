package io.github.lookoutldz.easyrequester.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
internal fun containsDataClassDeep(
    type: Type,
    visitedTypes: MutableSet<Type> = mutableSetOf(),
    maxDepth: Int = 10,
    currentDepth: Int = 0
): Boolean {
    // 防止无限递归
    if (currentDepth >= maxDepth || type in visitedTypes) {
        return false
    }
    
    visitedTypes.add(type)
    
    try {
        when (type) {
            is Class<*> -> {
                // 检查当前类型是否是 data class
                if (isDataClass(type)) {
                    return true
                }
                
                // 递归检查类的所有属性
                return checkClassPropertiesDeep(type, visitedTypes, maxDepth, currentDepth + 1)
            }
            
            is ParameterizedType -> {
                // 检查原始类型
                val rawType = type.rawType
                if (rawType is Class<*> && isDataClass(rawType)) {
                    return true
                }
                
                // 检查泛型参数
                for (typeArg in type.actualTypeArguments) {
                    if (containsDataClassDeep(typeArg, visitedTypes, maxDepth, currentDepth + 1)) {
                        return true
                    }
                }
                
                // 递归检查原始类型的属性
                if (rawType is Class<*>) {
                    return checkClassPropertiesDeep(rawType, visitedTypes, maxDepth, currentDepth + 1)
                }
            }
            
            is WildcardType -> {
                // 检查通配符类型的上界
                for (upperBound in type.upperBounds) {
                    if (containsDataClassDeep(upperBound, visitedTypes, maxDepth, currentDepth + 1)) {
                        return true
                    }
                }
                
                // 检查通配符类型的下界
                for (lowerBound in type.lowerBounds) {
                    if (containsDataClassDeep(lowerBound, visitedTypes, maxDepth, currentDepth + 1)) {
                        return true
                    }
                }
            }
        }
    } catch (e: Exception) {
        // 忽略反射异常，继续检查其他类型
    } finally {
        visitedTypes.remove(type)
    }
    
    return false
}

/**
 * 检查类的所有属性是否包含 data class
 */
private fun checkClassPropertiesDeep(
    clazz: Class<*>,
    visitedTypes: MutableSet<Type>,
    maxDepth: Int,
    currentDepth: Int
): Boolean {
    try {
        // 跳过基本类型和常见的系统类型
        if (clazz.isPrimitive || 
            clazz.packageName.startsWith("java.") == true ||
            clazz.packageName.startsWith("kotlin.") == true) {
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
            }
        }
        
        // 如果 Kotlin 反射失败，尝试使用 Java 反射
        for (field in clazz.declaredFields) {
            try {
                if (containsDataClassDeep(field.genericType, visitedTypes, maxDepth, currentDepth)) {
                    return true
                }
            } catch (e: Exception) {
                // 忽略单个字段的异常
            }
        }
    } catch (e: Exception) {
        // 忽略类级别的异常
    }
    
    return false
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
