# Easy Requester Wiki

A concise and elegant HTTP requester

## 🚀 Less Talk, More Code

Want to experience the charm of Easy Requester in the fastest way? Just one core line of code:
#### Kotlin

```kotlin
/**
 * doRequest(url) { do your business } 
 * Get user information, it's that simple!
 */
fun testRequest() {
    EasyHttpGet.doRequestDefault("https://api.github.com/users/octocat") { println("User info: $it") }
}
```
#### Java
```java
/**
 * doRequest(url, resp -> { do your business })
 * Get user information, it's that simple!
 */
public void testRequest() {
    EasyHttpGet4j.doRequestDefault("https://api.github.com/users/octocat", str -> System.out.println("User info: " + str));
}
```

## 📖 Project Overview
Easy Requester is a lightweight HTTP client library designed specifically for Java/Kotlin developers, built on the mature and stable OkHttp3.
Its design philosophy is: Elegant, Elegant, and damn Elegant!

### ✨ Core Features
- 🎯 Minimal API Design - Complete HTTP requests with as little as one line of code
- 🔄 Automatic JSON Processing - Built-in Jackson support for automatic serialization/deserialization
- 📦 Data Class Mapping - Perfect support for Kotlin Data Classes
- 🛠️ Flexible Configuration - Support for custom headers, parameters, and cookies
- 🎨 Functional Programming - Leverage Kotlin higher-order functions for more elegant code
- 🔧 Builder Pattern - Fluent interface for intuitive configuration

### 📦 Quick Installation 

#### Maven

``` xml
<dependency>
    <groupId>io.github.lookoutldz</groupId>
    <artifactId>easy-requester</artifactId>
    <version>2.3.3</version>
</dependency>
```

#### Gradle
Groovy by ``build.gradle``
```groovy
implementation 'io.github.lookoutldz:easy-requester:2.3.3'
```
Kotlin DSL by ``build.gradle.kts``
```kotlin
implementation("io.github.lookoutldz:easy-requester:2.3.3")
```

### 🎯 Master Core Usage in 1 Minute
#### 1. Using Three doRequest-style APIs, GET Example

Kotlin
```kotlin
val url = "https://api.github.com/users/octocat"
data class User(val login: String, val id: Int, val avatar_url: String)

// 1. Simplest GET request, returns response as String by default
EasyHttpGet.doRequestDefault(url) { str ->
    println(str)
}

// 2. Specify simple generic type to receive response (see Builder approach for complex types)
EasyHttpGet.doRequest<User>(url) { user ->
    println("Username: ${user?.login}, ID: ${user?.id}")
}

// 3. Use raw OkHttp Response to receive response
EasyHttpGet.doRequestRaw(url) { response ->
    println("${response.isSuccessful} - ${response.code} - ${response.message}")
}
```

Java
```java
public final String url = "https://api.github.com/users/octocat";

// 1. Simplest GET request, returns response as String by default
public void useDoRequestDefault() {
    EasyHttpGet4j.doRequestDefault(url, str -> System.out::println);
}

// 2. Specify clazz type to receive response
public void useDoRequest() {
    EasyHttpGet4j.doRequest(User.class, url, user -> System.out.println(user.id));
}

// 3. Use raw OkHttp Response to receive response
public void useDoRequestRaw() {
    EasyHttpGet4j.doRequestRaw(url, response -> 
            System.out.println(response.isSuccessful() + " - " + response.code() + " - " + response.message()));
}
```
#### 2. Using More Powerful Builder-style API
Kotlin Example
```kotlin
// Basic - Build request using Builder with clazz pattern
EasyHttpGet
    .Builder(ResponseBody::class.java)
    .setUrl(baseUrl)
    .onSuccess { responseBody ->
        // Define success callback function here
        println("ok - ${responseBody?.data}")
    }
    .build()
    .execute()  // don't forget to execute

// For complex return value types, use Builder with TypeReference
EasyHttpGet
    .Builder(object : TypeReference<ResponseBody<User>>() {})
    .setUrl(userUrl)
    .onSuccess { responseBody ->
        println("ok - ${responseBody?.data?.name}")
    }
    .build()
    .execute()
```

Java Example
```java
// Basic - Build request using Builder with clazz pattern
public void useBuilderPattern() {
    new EasyHttpGet4j.Builder<>(ResponseBody.class)
            .setUrl(baseUrl)
            .onSuccess(responseBody -> {
                // Define callback function here
                if (responseBody != null) {
                    System.out.println("ok - " + responseBody.getData());
                }
            })
            .build()
            .execute(); // don't forget to execute
}

// For complex return value types, use Builder with TypeReference
public void useBuilderPattern() {
    new EasyHttpGet4j.Builder<>(new TypeReference<ResponseBody<User>>() {})
            .setUrl(baseUrl)
            .onSuccess(responseBody -> {
                // Define callback function here
                if (responseBody != null) {
                    System.out.println("ok - " + responseBody.getData());
                }
            })
            .build()
            .execute(); // don't forget to execute
}
```

### 🌟 Why Choose Easy Requester?

| Traditional Approach | Easy Requester |
|:-------------------:|:--------------:|
| Manual JSON handling | ✅ Automatic serialization/deserialization |
| Complex exception handling | ✅ Elegant error handling mechanism |
| Verbose configuration code | ✅ One line of code for requests |
| Hard to test and maintain | ✅ Functional programming, easy to test |

Ready to dive deeper into advanced features? 👉 [Advanced Usage Guide](Advance_EN.md)
