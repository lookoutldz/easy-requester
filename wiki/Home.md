# Easy Requester Wiki

一款简洁、优雅的 HTTP 请求器

## 🚀 少废话，上代码

想要最快体验 Easy Requester 的魅力？最短只需要一行核心代码：
#### Kotlin

```kotlin
/**
 * doRequest(url) { do your business } 
 * 获取用户信息，就这么简单！
 */
fun testRequest() {
    EasyHttpGet.doRequestDefault("https://api.github.com/users/octocat") { println("用户信息: $it") }
}
```
#### Java
```java
/**
 * doRequest(url, resp -> { do your business })
 * 获取用户信息，就这么简单！
 */
public void testRequest() {
    EasyHttpGet4j.doRequestDefault("https://api.github.com/users/octocat", str -> System.out.println("用户信息: " + str));
}
```

## 📖 项目简介
Easy Requester 是一个专为 Java / Kotlin 开发者设计的轻量级 HTTP 客户端库，基于成熟稳定的 OkHttp3 构建。
它的设计理念是 优雅、优雅、还 TMD 是优雅！

### ✨ 核心特性
- 🎯 极简API设计 - 最短一行代码完成 HTTP 请求
- 🔄 自动JSON处理 - 内置 Jackson 支持，自动序列化/反序列化
- 📦 数据类映射 - 完美支持 Kotlin Data Class
- 🛠️ 灵活配置 - 支持自定义请求头、参数、Cookie
- 🎨 函数式编程 - 利用 Kotlin 高阶函数，代码更优雅
- 🔧 Builder模式 - 链式调用，配置更直观

### 📦 快速安装 

#### Maven

``` xml
<dependency>
    <groupId>io.github.lookoutldz</groupId>
    <artifactId>easy-requester</artifactId>
    <version>2.2</version>
</dependency>
```

#### Gradle
Groovy by ``build.gradle``
```groovy
implementation 'io.github.lookoutldz:easy-requester:2.2'
```
Kotlin DSL by ``build.gradle.kts``
```kotlin
implementation("io.github.lookoutldz:easy-requester:2.2")
```

### 🎯 1分钟掌握核心用法
#### 1. 使用三种 doRequest 式 API, 以 GET 为例

Kotlin
```kotlin
val url = "https://api.github.com/users/octocat"
data class User(val login: String, val id: Int, val avatar_url: String)

// 1. 最简单的GET请求，默认将返回信息 String 化
EasyHttpGet.doRequestDefault(url) { str ->
    println(str)
}

// 2. 指定简单泛型接收响应(复杂类型详见 Builder 方式)
EasyHttpGet.doRequest<User>(url) { user ->
    println("用户名：${user?.login}，ID：${user?.id}")
}

// 3. 使用原始 OkHttp Response 接收响应
EasyHttpGet.doRequestRaw(url) { response ->
    println("${response.isSuccessful} - ${response.code} - ${response.message}")
}
```

Java
```java
public final String url = "https://api.github.com/users/octocat";

// 1. 最简单的GET请求，默认将返回信息 String 化
public void useDoRequestDefault() {
    EasyHttpGet4j.doRequestDefault(url, str -> System.out::println);
}

// 2. 指定 clazz 类型接收响应
public void useDoRequest() {
    EasyHttpGet4j.doRequest(User.class, url, user -> System.out.println(user.id));
}

// 2. 使用原始 OkHttp Response 接收响应
public void useDoRequestRaw() {
    EasyHttpGet4j.doRequestRaw(url, response -> 
            System.out.println(response.isSuccessful() + " - " + response.code() + " - " + response.message()));
}
```
#### 2. 使用更强大的 Builder 式 API
Kotlin示例
```kotlin
// 基础 - 使用 Builder with clazz 模式构建请求
EasyHttpGet
    .Builder(ResponseBody::class.java)
    .setUrl(baseUrl)
    .onSuccess { responseBody ->
        // 在这里定义成功回调函数
        println("ok - ${responseBody?.data}")
    }
    .build()
    .execute()  // don't forget to execute

// 针对复杂的返回值类型，使用 Builder with TypeReference 指定
EasyHttpGet
    .Builder(object : TypeReference<ResponseBody<User>>() {})
    .setUrl(userUrl)
    .onSuccess { responseBody ->
        println("ok - ${responseBody?.data?.name}")
    }
    .build()
    .execute()
```

Java 示例
```java
// 基础 - 使用 Builder with clazz 模式构建请求
public void useBuilderPattern() {
    new EasyHttpGet4j.Builder<>(ResponseBody.class)
            .setUrl(baseUrl)
            .onSuccess(responseBody -> {
                // 这里定义回调函数
                if (responseBody != null) {
                    System.out.println("ok - " + responseBody.getData());
                }
            })
            .build()
            .execute(); // don't forget to execute
}

// 针对复杂的返回值类型，使用 Builder with TypeReference 指定
public void useBuilderPattern() {
    new EasyHttpGet4j.Builder<>(new TypeReference<ResponseBody<User>>() {})
            .setUrl(baseUrl)
            .onSuccess(responseBody -> {
                // 这里定义回调函数
                if (responseBody != null) {
                    System.out.println("ok - " + responseBody.getData());
                }
            })
            .build()
            .execute(); // don't forget to execute
}
```

### 🌟 为什么选择Easy Requester？

|    传统方式    | Easy Requester |
|:----------:|:--------------:|
| 需要手动处理JSON |  ✅ 自动序列化/反序列化  |
|  复杂的异常处理   |  ✅ 优雅的错误处理机制   |
|  冗长的配置代码   |   ✅ 一行代码完成请求   |
|  难以测试和维护   |  ✅ 函数式编程，易测试   |

准备好深入了解更多高级功能了吗？ 👉 [进阶用法指南](Advance.md)