# Easy Requester - 进阶用法指南

## 🔧 高级配置

### 自定义请求头和认证

**Kotlin版本：**
```kotlin
// JWT认证示例
val headers = mapOf(
    "Authorization" to "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "Content-Type" to "application/json",
    "User-Agent" to "MyApp/1.0"
)

EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/protected/data",
    headers = headers
) { response ->
    // 处理受保护的数据
}
```
**Java版本：**
```java
// JWT认证示例
public void foobar() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
    headers.put("Content-Type", "application/json");
    headers.put("User-Agent", "MyApp/1.0");

    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/protected/data")
            .setHeaders(headers)
            .onSuccess(response -> {
                // 处理受保护的数据
            });
    builder.build().execute();

    // 或者使用简化方法
    EasyHttpGet4j.doRequest(
            ApiResponse.class,
            "https://api.example.com/protected/data",
            response -> {
                // 处理受保护的数据
            }
    );
}
```

### 请求参数和查询字符串
**Kotlin版本：**
```kotlin
// 复杂查询参数
val params = mapOf(
    "page" to "1",
    "size" to "20",
    "sort" to "created_at",
    "order" to "desc",
    "filter" to "active"
)

EasyHttpGet.doRequest<PagedResult<User>>(
    url = "https://api.example.com/users",
    params = params
) { pagedUsers ->
    println("总共 ${pagedUsers?.total} 个用户")
    pagedUsers?.data?.forEach { user ->
        println("- ${user.name}")
    }
}
```
**Java版本：**
```java
// 复杂查询参数
public void foobar() {
    Map<String, String> params = new HashMap<>();
    params.put("page", "1");
    params.put("size", "20");
    params.put("sort", "created_at");
    params.put("order", "desc");
    params.put("filter", "active");

    // 使用TypeReference处理泛型
    TypeReference<PagedResult<User>> typeRef = new TypeReference<PagedResult<User>>() {};

    EasyHttpGet4j.Builder<PagedResult<User>> builder = new EasyHttpGet4j.Builder<>(typeRef)
            .setUrl("https://api.example.com/users")
            .setParams(params)
            .onSuccess(pagedUsers -> {
                System.out.println("总共 " + (pagedUsers != null ? pagedUsers.getTotal() : 0) + " 个用户");
                if (pagedUsers != null && pagedUsers.getData() != null) {
                    pagedUsers.getData().forEach(user -> {
                        System.out.println("- " + user.getName());
                    });
                }
            });
    builder.build().execute();
}
```

### Cookie管理
**Kotlin版本：**
```kotlin
// 会话管理
val sessionCookies = mapOf(
    "JSESSIONID" to "ABC123DEF456",
    "remember_token" to "user_token_here"
)

EasyHttpGet.doRequest<UserProfile>(
    url = "https://api.example.com/profile",
    cookies = sessionCookies
) { profile ->
    println("欢迎回来，${profile?.name}！")
}
```
**Java版本：**
```java
public void foobar() {
    // 会话管理
    Map<String, String> sessionCookies = new HashMap<>();
    sessionCookies.put("JSESSIONID", "ABC123DEF456");
    sessionCookies.put("remember_token", "user_token_here");

    EasyHttpGet4j.Builder<UserProfile> builder = new EasyHttpGet4j.Builder<>(UserProfile.class)
            .setUrl("https://api.example.com/profile")
            .setCookies(sessionCookies)
            .onSuccess(profile -> {
                System.out.println("欢迎回来，" + (profile != null ? profile.getName() : "未知用户") + "！");
            });
    builder.build().execute();
}
```

## 🎯 不同HTTP方法的使用
### POST请求 - 创建资源
**Kotlin版本：**
```kotlin
// 创建博客文章
data class Article(
    val title: String,
    val content: String,
    val tags: List<String>,
    val published: Boolean = false
)
val newArticle = Article(
    title = "Easy Requester使用指南",
    content = "这是一篇关于如何使用Easy Requester的文章...",
    tags = listOf("kotlin", "http", "tutorial")
)
EasyHttpPost.doRequest<Article>(
    url = "https://api.blog.com/articles",
    body = newArticle,
    headers = mapOf("Authorization" to "Bearer $token")
) { createdArticle ->
    println("文章创建成功，ID: ${createdArticle?.id}")
}
```
**Java版本：**
```java
// 创建博客文章
public class Article {
    private String title;
    private String content;
    private List<String> tags;
    private boolean published = false;
    
    // 构造函数、getter和setter省略
}

public void foobar() {
    Article newArticle = new Article();
    newArticle.setTitle("Easy Requester使用指南");
    newArticle.setContent("这是一篇关于如何使用Easy Requester的文章...");
    newArticle.setTags(Arrays.asList("java", "http", "tutorial"));

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + token);

    EasyHttpPost4j.Builder<Article> builder = new EasyHttpPost4j.Builder<>(Article.class)
            .setUrl("https://api.blog.com/articles")
            .setBody(newArticle)
            .setContentType("application/json")
            .setHeaders(headers)
            .onSuccess(createdArticle -> {
                System.out.println("文章创建成功，ID: " + (createdArticle != null ? createdArticle.getId() : "未知"));
            });
    builder.build().execute();

    // 或者使用简化方法
    EasyHttpPost4j.doRequest(
            Article.class,
            "https://api.blog.com/articles",
            newArticle,
            "application/json",
            createdArticle -> {
                System.out.println("文章创建成功，ID: " + (createdArticle != null ? createdArticle.getId() : "未知"));
            }
    );
}
```

### PUT请求 - 更新资源
**Kotlin版本：**
```kotlin
// 更新用户信息
data class UserUpdate(
    val name: String,
    val email: String,
    val bio: String
)

val userUpdate = UserUpdate(
    name = "张三",
    email = "zhangsan@newdomain.com",
    bio = "Kotlin开发者，热爱开源"
)

EasyHttpPut.doRequest<User>(
    url = "https://api.example.com/users/123",
    body = userUpdate
) { updatedUser ->
    println("用户信息更新成功：${updatedUser?.name}")
}
```
**Java版本：**
```java
// 更新用户信息
public class UserUpdate {
    private String name;
    private String email;
    private String bio;
    
    // 构造函数、getter和setter省略
}

public void foobar() {
    UserUpdate userUpdate = new UserUpdate();
    userUpdate.setName("张三");
    userUpdate.setEmail("zhangsan@newdomain.com");
    userUpdate.setBio("Java开发者，热爱开源");

    // 注意：这里假设有EasyHttpPut4j类，实际使用时需要确认是否存在
    EasyHttpPut4j.doRequest(
            User.class,
            "https://api.example.com/users/123",
            userUpdate,
            "application/json",
            updatedUser -> {
                System.out.println("用户信息更新成功：" + (updatedUser != null ? updatedUser.getName() : "未知"));
            }
    );
}
```

### DELETE请求 - 删除资源
**Kotlin版本：**
```kotlin
// 删除文章
EasyHttpDelete.doRequestDefault(
    url = "https://api.blog.com/articles/456",
    headers = mapOf("Authorization" to "Bearer $token")
) { response ->
    if (response.contains("success")) {
        println("文章删除成功")
    }
}
```
**Java版本：**
```java
public void foobar() {
    // 删除文章
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + token);

// 注意：这里假设有EasyHttpDelete4j类，实际使用时需要确认是否存在
    EasyHttpDelete4j.doRequestDefault(
            "https://api.blog.com/articles/456",
            response -> {
                if (response != null && response.contains("success")) {
                    System.out.println("文章删除成功");
                }
            }
    );
}
```

## 🛡️ 错误处理和重试机制
### 自定义异常处理
**Kotlin版本：**
```kotlin
EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/data",
    exceptionHandler = { throwable, request ->
        when (throwable) {
            is java.net.SocketTimeoutException -> {
                println("请求超时，请检查网络连接")
                // 可以实现重试逻辑
            }
            is java.net.UnknownHostException -> {
                println("无法连接到服务器，请检查URL")
            }
            else -> {
                println("请求失败: ${throwable?.message}")
                // 记录错误日志
            }
        }
    }
) { response ->
    // 成功处理逻辑
}
```
**Java版本：**
```java
public void foobar () {
    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/data")
            .onSuccess(response -> {
                // 成功处理逻辑
            })
            .onException((throwable, request) -> {
                if (throwable instanceof java.net.SocketTimeoutException) {
                    System.out.println("请求超时，请检查网络连接");
                    // 可以实现重试逻辑
                } else if (throwable instanceof java.net.UnknownHostException) {
                    System.out.println("无法连接到服务器，请检查URL");
                } else {
                    System.out.println("请求失败: " + (throwable != null ? throwable.getMessage() : "未知错误"));
                    // 记录错误日志
                }
            });
    
    builder.build().execute();
}
```

### 响应状态码处理
**Kotlin版本：**
```kotlin
EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/data",
    responseSuccessHandler = { response ->
        println("请求成功，状态码: ${response.code}")
    },
    responseFailureHandler = { response ->
        when (response.code) {
            401 -> println("认证失败，请重新登录")
            403 -> println("权限不足")
            404 -> println("资源不存在")
            500 -> println("服务器内部错误")
            else -> println("请求失败，状态码: ${response.code}")
        }
    }
) { data ->
    // 处理成功响应的数据
}
```
**Java版本：**
```java
public void foobar() {
    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/data")
            .onResponseSuccess(response -> {
                System.out.println("请求成功，状态码: " + response.code());
            })
            .onResponseFailure(response -> {
                switch (response.code()) {
                    case 401:
                        System.out.println("认证失败，请重新登录");
                        break;
                    case 403:
                        System.out.println("权限不足");
                        break;
                    case 404:
                        System.out.println("资源不存在");
                        break;
                    case 500:
                        System.out.println("服务器内部错误");
                        break;
                    default:
                        System.out.println("请求失败，状态码: " + response.code());
                        break;
                }
            })
            .onSuccess(data -> {
                // 处理成功响应的数据
            });
    
    builder.build().execute();
}
```

## ⚙️ 自定义OkHttpClient
### 配置超时和连接池
**Kotlin版本：**
```kotlin
//import okhttp3.OkHttpClient
//import java.util.concurrent.TimeUnit

// 创建自定义的OkHttpClient
val customClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

// 使用自定义客户端
EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/large-data",
    okHttpClient = customClient
) { response ->
    // 处理大数据响应
}
```
**Java版本：**
```java
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public void foobar() {
    // 创建自定义的OkHttpClient
    OkHttpClient customClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    // 使用自定义客户端
    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/large-data")
            .setOkHttpClient(customClient)
            .onSuccess(response -> {
                // 处理大数据响应
            });
    
    builder.build().execute();
}
```

### 添加拦截器
**Kotlin版本：**
```kotlin
//import okhttp3.Interceptor
//import okhttp3.logging.HttpLoggingInterceptor

// 日志拦截器
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

// 自定义请求头拦截器
val headerInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    val requestWithHeaders = originalRequest.newBuilder()
        .header("X-API-Version", "v1")
        .header("X-Client-Type", "mobile")
        .build()
    chain.proceed(requestWithHeaders)
}

val clientWithInterceptors = OkHttpClient.Builder()
    .addInterceptor(headerInterceptor)
    .addInterceptor(loggingInterceptor)
    .build()

EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/data",
    okHttpClient = clientWithInterceptors
) { response ->
    // 所有请求都会自动添加版本和客户端类型头
}
```
**Java版本：**
```java
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

public void foobar() {
    // 日志拦截器
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    // 自定义请求头拦截器
    Interceptor headerInterceptor = chain -> {
        okhttp3.Request originalRequest = chain.request();
        okhttp3.Request requestWithHeaders = originalRequest.newBuilder()
                .header("X-API-Version", "v1")
                .header("X-Client-Type", "mobile")
                .build();
        return chain.proceed(requestWithHeaders);
    };

    OkHttpClient clientWithInterceptors = new OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build();

    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/data")
            .setOkHttpClient(clientWithInterceptors)
            .onSuccess(response -> {
                // 所有请求都会自动添加版本和客户端类型头
            });
    
    builder.build().execute();
}
```

## 🔄 自定义JSON序列化
### 配置Jackson ObjectMapper
**Kotlin版本：**
```kotlin
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.PropertyNamingStrategies
//import com.fasterxml.jackson.module.kotlin.registerKotlinModule

// 自定义ObjectMapper
val customMapper = ObjectMapper().apply {
    registerKotlinModule()
    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    // 忽略未知属性
    configure(com.fasterxml.jackson.databind.DeserializationFeature.
    FAIL_ON_UNKNOWN_PROPERTIES, false)
}

// 使用自定义序列化器
EasyHttpPost.doRequest<User>(
    url = "https://api.example.com/users",
    body = newUser,
    objectMapper = customMapper
) { user ->
    // 使用snake_case命名策略处理JSON
}
```
**Java版本：**
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.DeserializationFeature;

public void foobar() {
    // 自定义ObjectMapper
    ObjectMapper customMapper = new ObjectMapper();
    customMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    // 忽略未知属性
    customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // 使用自定义序列化器
    EasyHttpPost4j.Builder<User> builder = new EasyHttpPost4j.Builder<>(User.class)
            .setUrl("https://api.example.com/users")
            .setBody(newUser)
            .setContentType("application/json")
            .setObjectMapper(customMapper)
            .onSuccess(user -> {
                // 使用snake_case命名策略处理JSON
            });
    builder.build().execute();
}
```

## 🧪 测试和调试
### 原始响应处理
**Kotlin版本：**
```kotlin
// 获取原始响应进行调试
EasyHttpGet.doRequestRaw(
    url = "https://api.example.com/debug"
) { response ->
    println("状态码: ${response.code}")
    println("响应头: ${response.headers}")
    println("响应体: ${response.body?.string()}")
}
```
**Java版本：**
```java
public void foobar() {
    // 获取原始响应进行调试
    EasyHttpGet4j.doRequestRaw(
            "https://api.example.com/debug",
            response -> {
                System.out.println("状态码: " + response.code());
                System.out.println("响应头: " + response.headers());
                try {
                    if (response.body() != null) {
                        System.out.println("响应体: " + response.body().string());
                    }
                } catch (Exception e) {
                    System.err.println("读取响应体失败: " + e.getMessage());
                }
            }
    );
}
```

### Builder模式进行复杂配置
**Kotlin版本：**
```kotlin
// 使用Builder模式进行复杂配置
EasyHttpPost.Builder<ApiResponse>(ApiResponse::class.java)
    .setUrl("https://api.example.com/complex")
    .setBody(complexData)
    .setHeaders(mapOf("Authorization" to "Bearer $token"))
    .setParams(mapOf("version" to "v2"))
    .setCookies(sessionCookies)
    .setOkHttpClient(customClient)
    .setObjectMapper(customMapper)
    .onSuccess { response ->
        println("成功: $response")
    }
    .onException { error, request ->
        println("错误: ${error?.message}")
    }
    .build()
    .execute()
```
**Java版本：**
```java
public void foobar() {
    // 使用Builder模式进行复杂配置
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + token);

    Map<String, String> params = new HashMap<>();
    params.put("version", "v2");

    EasyHttpPost4j.Builder<ApiResponse> builder = new EasyHttpPost4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/complex")
            .setBody(complexData)
            .setContentType("application/json")
            .setHeaders(headers)
            .setParams(params)
            .setCookies(sessionCookies)
            .setOkHttpClient(customClient)
            .setObjectMapper(customMapper)
            .onSuccess(response -> {
                System.out.println("成功: " + response);
            })
            .onException((error, request) -> {
                System.out.println("错误: " + (error != null ? error.getMessage() : "未知错误"));
            });
    
    builder.build().execute();
}
```

## 🚀 最佳实践
### 1. 创建API客户端类
**Kotlin版本：**
```kotlin
class GitHubApiClient {
    private val baseUrl = "https://api.github.com"
    private val headers = mapOf(
        "Accept" to "application/vnd.github.v3+json",
        "User-Agent" to "MyApp/1.0"
    )
    
    fun getUser(username: String, callback: (User?) -> Unit) {
        EasyHttpGet.doRequest<User>(
            url = "$baseUrl/users/$username",
            headers = headers,
            successHandler = callback
        )
    }
    
    fun getUserRepos(username: String, callback: (List<Repository>?) -> Unit) {
        EasyHttpGet.doRequest<List<Repository>>(
            url = "$baseUrl/users/$username/repos",
            headers = headers,
            successHandler = callback
        )
    }
}
```
**Java版本：**
```java
public class GitHubApiClient {
    private final String baseUrl = "https://api.github.com";
    private final Map<String, String> headers;
    
    public GitHubApiClient() {
        headers = new HashMap<>();
        headers.put("Accept", "application/vnd.github.v3+json");
        headers.put("User-Agent", "MyApp/1.0");
    }
    
    public void getUser(String username, SuccessHandler<User> callback) {
        EasyHttpGet4j.Builder<User> builder = new EasyHttpGet4j.Builder<>(User.class)
            .setUrl(baseUrl + "/users/" + username)
            .setHeaders(headers)
            .onSuccess(callback);
        builder.build().execute();
    }
    
    public void getUserRepos(String username, SuccessHandler<List<Repository>> callback) {
        TypeReference<List<Repository>> typeRef = new TypeReference<List<Repository>>() {};
        EasyHttpGet4j.Builder<List<Repository>> builder = new EasyHttpGet4j.Builder<>(typeRef)
            .setUrl(baseUrl + "/users/" + username + "/repos")
            .setHeaders(headers)
            .onSuccess(callback);
        builder.build().execute();
    }
}
```

### 2. 统一错误处理
**Kotlin版本：**
```kotlin
object ApiErrorHandler {
    fun handleError(throwable: Throwable?, request: okhttp3.Request) {
        // 统一的错误处理逻辑
        when (throwable) {
            is java.net.SocketTimeoutException -> {
                // 显示超时提示
            }
            is java.net.UnknownHostException -> {
                // 显示网络错误提示
            }
            else -> {
                // 记录错误日志
                println("API请求失败: ${request.url}, 错误: ${throwable?.message}")
            }
        }
    }
}

// 在所有请求中使用统一错误处理
EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/user",
    exceptionHandler = ApiErrorHandler::handleError
) { user ->
    // 处理用户数据
}
```
**Java版本：**
```java
public class ApiErrorHandler {
    public static void handleError(Throwable throwable, okhttp3.Request request) {
        // 统一的错误处理逻辑
        if (throwable instanceof java.net.SocketTimeoutException) {
            // 显示超时提示
            System.out.println("请求超时");
        } else if (throwable instanceof java.net.UnknownHostException) {
            // 显示网络错误提示
            System.out.println("网络连接失败");
        } else {
            // 记录错误日志
            System.out.println("API请求失败: " + request.url() + ", 错误: " + 
                (throwable != null ? throwable.getMessage() : "未知错误"));
        }
    }
}

public void foobar() {
    // 在所有请求中使用统一错误处理
    EasyHttpGet4j.Builder<User> builder = new EasyHttpGet4j.Builder<>(User.class)
            .setUrl("https://api.example.com/user")
            .onException(ApiErrorHandler::handleError)
            .onSuccess(user -> {
                // 处理用户数据
            });
    builder.build().execute();
}
```

### 3. 环境配置管理
**Kotlin版本：**
```kotlin
object ApiConfig {
    private val isDevelopment = BuildConfig.DEBUG
    
    val baseUrl = if (isDevelopment) {
        "https://api-dev.example.com"
    } else {
        "https://api.example.com"
    }
    
    val defaultHeaders = mapOf(
        "Content-Type" to "application/json",
        "X-API-Version" to "v1"
    )
    
    val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .apply {
            if (isDevelopment) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()
}
```
**Java版本：**
```java
public class ApiConfig {
    private static final boolean isDevelopment = BuildConfig.DEBUG;
    
    public static final String baseUrl = isDevelopment ? 
        "https://api-dev.example.com" : "https://api.example.com";
    
    public static final Map<String, String> defaultHeaders;
    static {
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-API-Version", "v1");
    }
    
    public static final OkHttpClient httpClient;
    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS);
            
        if (isDevelopment) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        
        httpClient = builder.build();
    }
}
```