### [English](README.md) | 中文

# Easy Requester

Easy Requester 是一个轻量级的 Java/Kotlin HTTP 客户端库，它基于 OkHttp3 构建，提供了简单易用的 API 来发送 HTTP 请求。


<div align="center">

[![Maven Central](https://img.shields.io/maven-central/v/io.github.lookoutldz/easy-requester.svg)](https://central.sonatype.com/artifact/io.github.lookoutldz/easy-requester)
[![GitHub](https://img.shields.io/github/license/lookoutldz/easy-requester.svg)](https://github.com/lookoutldz/easy-requester/blob/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/lookoutldz/easy-requester.svg?style=social)](https://github.com/lookoutldz/easy-requester)

</div>

## 特性

- 简洁的 API 设计，易于使用
- 支持同步 HTTP 请求
- 支持自定义请求参数、头信息和 Cookie
- 支持自动序列化和反序列化 JSON 数据（使用 Jackson）
- 支持 Kotlin 数据类的自动映射
- 支持自定义响应处理和异常处理
- 使用链式调用风格的 Builder 模式
- 使用 Kotlin 高阶函数的函数式编程模式

## 安装

### Maven

在你的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.lookoutldz</groupId>
    <artifactId>easy-requester</artifactId>
    <version>2.3</version>
</dependency>
```

### Gradle

在你的 `build.gradle` 文件中添加以下依赖：

```groovy
implementation 'io.github.lookoutldz:easy-requester:2.3'
```

如果使用的是 Kotlin DSL, 则是在 ``build.gradle.kts`` 中添加依赖：

```kotlin
implementation("io.github.lookoutldz:easy-requester:2.3")
```

## 快速开始

### 基本用法

```kotlin
// 最简单的方式 - 返回字符串
EasyHttpGet.doRequestDefault(url = "https://api.example.com") { response ->
    println("响应内容: $response")
}

// 使用数据类接收响应
data class User(val id: Int, val name: String)

EasyHttpGet.doRequest<User>(url = "https://api.example.com/users/1") { user ->
    println("用户名: ${user?.name}")
}
```

### 添加请求参数

```kotlin
// 添加查询参数
val params = mapOf("page" to "1", "limit" to "10")

EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/users",
    params = params
) { users ->
    println("获取到 ${users?.size} 个用户")
}
```

### 添加请求头和 Cookie

```kotlin
val headers = mapOf("Authorization" to "Bearer token123")
val cookies = mapOf("sessionId" to "abc123")

EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/users",
    headers = headers,
    cookies = cookies
) { user ->
    println("用户信息: $user")
}
```

### 自定义异常处理

```kotlin
EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/users/1",
    exceptionHandler = { throwable, request ->
        println("请求失败: ${request.url}, 错误: ${throwable?.message}")
        // 在这里处理异常，例如重试或记录日志
    }
) { user ->
    println("用户信息: $user")
}
```

### 处理原始响应

```kotlin
EasyHttpGet.doRequestRaw(url = "https://api.example.com") { response ->
    println("状态码: ${response.code}")
    println("响应头: ${response.headers}")
    println("响应体: ${response.body?.string()}")
}
```

### 使用 Builder 模式

```kotlin
// 使用 Class 指定返回类型
EasyHttpGet
    .Builder(User::class.java)
    .setUrl("https://api.example.com/users/1")
    .setHeaders(mapOf("Authorization" to "Bearer token123"))
    .onSuccess { user ->
        println("用户名: ${user?.name}")
    }
    .onException { throwable, request ->
        println("请求失败: ${throwable.message}")
    }
    .build()
    .execute()

// 使用 TypeReference 处理泛型类型
EasyHttpGet
    .Builder(object : TypeReference<List<User>>() {})
    .setUrl("https://api.example.com/users")
    .setParams(mapOf("page" to "1"))
    .onSuccess { users ->
        println("用户列表: $users")
    }
    .build()
    .execute()
```

### 自定义 ObjectMapper

```kotlin
val objectMapper = ObjectMapper().registerKotlinModule()
// 可以在这里配置 ObjectMapper，例如日期格式、序列化选项等

EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/users/1",
    objectMapper = objectMapper
) { user ->
    println("用户信息: $user")
}
```

## 高级用法

### 自定义响应处理

```kotlin
EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/users/1",
    responseSuccessHandler = { response ->
        // 处理成功的响应
        println("成功: ${response.code}")
        // 可以在这里执行自定义逻辑，例如提取特定的头信息
    },
    responseFailureHandler = { response ->
        // 处理失败的响应
        println("失败: ${response.code} - ${response.message}")
    }
) { user ->
    println("用户信息: $user")
}
```

### 自定义 OkHttpClient

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/users/1",
    okHttpClient = okHttpClient
) { user ->
    println("用户信息: $user")
}
```

## 注意事项

1. 当处理 Kotlin 数据类时，请确保使用带有 Kotlin 模块的 ObjectMapper：
   ```kotlin
   ObjectMapper().registerKotlinModule()
   ```

2. 默认情况下，库会自动检测是否需要 Kotlin 模块，但在某些情况下可能需要手动指定。

3. 所有请求都是同步的，如果需要异步操作，请考虑在协程或线程中执行。

## 许可证

[MIT License](./LICENSE)

## 贡献

欢迎提交 Pull Request 和 Issue！
