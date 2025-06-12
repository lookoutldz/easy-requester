# Easy Requester - Advanced Usage Guide

## 🔧 Advanced Configuration

### Custom Headers and Authentication

**Kotlin Version:**
```kotlin
// JWT authentication example
val headers = mapOf(
    "Authorization" to "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "Content-Type" to "application/json",
    "User-Agent" to "MyApp/1.0"
)

EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/protected/data",
    headers = headers
) { response ->
    // Handle protected data
}
```
**Java Version:**
```java
// JWT authentication example
public void foobar() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
    headers.put("Content-Type", "application/json");
    headers.put("User-Agent", "MyApp/1.0");

    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/protected/data")
            .setHeaders(headers)
            .onSuccess(response -> {
                // Handle protected data
            });
    builder.build().execute();

    // Or use simplified method
    EasyHttpGet4j.doRequest(
            ApiResponse.class,
            "https://api.example.com/protected/data",
            response -> {
                // Handle protected data
            }
    );
}
```

### Request Parameters and Query Strings
**Kotlin Version:**
```kotlin
// Complex query parameters
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
    println("Total ${pagedUsers?.total} users")
    pagedUsers?.data?.forEach { user ->
        println("- ${user.name}")
    }
}
```
**Java Version:**
```java
// Complex query parameters
public void foobar() {
    Map<String, String> params = new HashMap<>();
    params.put("page", "1");
    params.put("size", "20");
    params.put("sort", "created_at");
    params.put("order", "desc");
    params.put("filter", "active");

    // Use TypeReference to handle generics
    TypeReference<PagedResult<User>> typeRef = new TypeReference<PagedResult<User>>() {};

    EasyHttpGet4j.Builder<PagedResult<User>> builder = new EasyHttpGet4j.Builder<>(typeRef)
            .setUrl("https://api.example.com/users")
            .setParams(params)
            .onSuccess(pagedUsers -> {
                System.out.println("Total " + (pagedUsers != null ? pagedUsers.getTotal() : 0) + " users");
                if (pagedUsers != null && pagedUsers.getData() != null) {
                    pagedUsers.getData().forEach(user -> {
                        System.out.println("- " + user.getName());
                    });
                }
            });
    builder.build().execute();
}
```

### Cookie Management
**Kotlin Version:**
```kotlin
// Session management
val sessionCookies = mapOf(
    "JSESSIONID" to "ABC123DEF456",
    "remember_token" to "user_token_here"
)

EasyHttpGet.doRequest<UserProfile>(
    url = "https://api.example.com/profile",
    cookies = sessionCookies
) { profile ->
    println("Welcome back, ${profile?.name}!")
}
```
**Java Version:**
```java
public void foobar() {
    // Session management
    Map<String, String> sessionCookies = new HashMap<>();
    sessionCookies.put("JSESSIONID", "ABC123DEF456");
    sessionCookies.put("remember_token", "user_token_here");

    EasyHttpGet4j.Builder<UserProfile> builder = new EasyHttpGet4j.Builder<>(UserProfile.class)
            .setUrl("https://api.example.com/profile")
            .setCookies(sessionCookies)
            .onSuccess(profile -> {
                System.out.println("Welcome back, " + (profile != null ? profile.getName() : "unknown user") + "!");
            });
    builder.build().execute();
}
```

## 🎯 Using Different HTTP Methods
### POST Request - Creating Resources
**Kotlin Version:**
```kotlin
// Create blog article
data class Article(
    val title: String,
    val content: String,
    val tags: List<String>,
    val published: Boolean = false
)
val newArticle = Article(
    title = "Easy Requester Usage Guide",
    content = "This is an article about how to use Easy Requester...",
    tags = listOf("kotlin", "http", "tutorial")
)
EasyHttpPost.doRequest<Article>(
    url = "https://api.blog.com/articles",
    body = newArticle,
    headers = mapOf("Authorization" to "Bearer $token")
) { createdArticle ->
    println("Article created successfully, ID: ${createdArticle?.id}")
}
```
**Java Version:**
```java
// Create blog article
public class Article {
    private String title;
    private String content;
    private List<String> tags;
    private boolean published = false;
    
    // Constructor, getters and setters omitted
}

public void foobar() {
    Article newArticle = new Article();
    newArticle.setTitle("Easy Requester Usage Guide");
    newArticle.setContent("This is an article about how to use Easy Requester...");
    newArticle.setTags(Arrays.asList("java", "http", "tutorial"));

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + token);

    EasyHttpPost4j.Builder<Article> builder = new EasyHttpPost4j.Builder<>(Article.class)
            .setUrl("https://api.blog.com/articles")
            .setBody(newArticle)
            .setContentType("application/json")
            .setHeaders(headers)
            .onSuccess(createdArticle -> {
                System.out.println("Article created successfully, ID: " + (createdArticle != null ? createdArticle.getId() : "unknown"));
            });
    builder.build().execute();

    // Or use simplified method
    EasyHttpPost4j.doRequest(
            Article.class,
            "https://api.blog.com/articles",
            newArticle,
            "application/json",
            createdArticle -> {
                System.out.println("Article created successfully, ID: " + (createdArticle != null ? createdArticle.getId() : "unknown"));
            }
    );
}
```

### PUT Request - Updating Resources
**Kotlin Version:**
```kotlin
// Update user information
data class UserUpdate(
    val name: String,
    val email: String,
    val bio: String
)

val userUpdate = UserUpdate(
    name = "John Doe",
    email = "john@newdomain.com",
    bio = "Kotlin developer, loves open source"
)

EasyHttpPut.doRequest<User>(
    url = "https://api.example.com/users/123",
    body = userUpdate
) { updatedUser ->
    println("User information updated successfully: ${updatedUser?.name}")
}
```
**Java Version:**
```java
// Update user information
public class UserUpdate {
    private String name;
    private String email;
    private String bio;
    
    // Constructor, getters and setters omitted
}

public void foobar() {
    UserUpdate userUpdate = new UserUpdate();
    userUpdate.setName("John Doe");
    userUpdate.setEmail("john@newdomain.com");
    userUpdate.setBio("Java developer, loves open source");

    // Note: Assuming EasyHttpPut4j class exists, please confirm when using
    EasyHttpPut4j.doRequest(
            User.class,
            "https://api.example.com/users/123",
            userUpdate,
            "application/json",
            updatedUser -> {
                System.out.println("User information updated successfully: " + (updatedUser != null ? updatedUser.getName() : "unknown"));
            }
    );
}
```

### DELETE Request - Deleting Resources
**Kotlin Version:**
```kotlin
// Delete article
EasyHttpDelete.doRequestDefault(
    url = "https://api.blog.com/articles/456",
    headers = mapOf("Authorization" to "Bearer $token")
) { response ->
    if (response.contains("success")) {
        println("Article deleted successfully")
    }
}
```
**Java Version:**
```java
public void foobar() {
    // Delete article
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + token);

    // Note: Assuming EasyHttpDelete4j class exists, please confirm when using
    EasyHttpDelete4j.doRequestDefault(
            "https://api.blog.com/articles/456",
            response -> {
                if (response != null && response.contains("success")) {
                    System.out.println("Article deleted successfully");
                }
            }
    );
}
```

## 🛡️ Error Handling and Retry Mechanisms
### Custom Exception Handling
**Kotlin Version:**
```kotlin
EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/data",
    exceptionHandler = { throwable, request ->
        when (throwable) {
            is java.net.SocketTimeoutException -> {
                println("Request timeout, please check network connection")
                // Can implement retry logic
            }
            is java.net.UnknownHostException -> {
                println("Cannot connect to server, please check URL")
            }
            else -> {
                println("Request failed: ${throwable?.message}")
                // Log error
            }
        }
    }
) { response ->
    // Success handling logic
}
```
**Java Version:**
```java
public void foobar() {
    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/data")
            .onSuccess(response -> {
                // Success handling logic
            })
            .onException((throwable, request) -> {
                if (throwable instanceof java.net.SocketTimeoutException) {
                    System.out.println("Request timeout, please check network connection");
                    // Can implement retry logic
                } else if (throwable instanceof java.net.UnknownHostException) {
                    System.out.println("Cannot connect to server, please check URL");
                } else {
                    System.out.println("Request failed: " + (throwable != null ? throwable.getMessage() : "unknown error"));
                    // Log error
                }
            });
    
    builder.build().execute();
}
```

### Response Status Code Handling
**Kotlin Version:**
```kotlin
EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/data",
    responseSuccessHandler = { response ->
        println("Request successful, status code: ${response.code}")
    },
    responseFailureHandler = { response ->
        when (response.code) {
            401 -> println("Authentication failed, please login again")
            403 -> println("Insufficient permissions")
            404 -> println("Resource not found")
            500 -> println("Internal server error")
            else -> println("Request failed, status code: ${response.code}")
        }
    }
) { data ->
    // Handle successful response data
}
```
**Java Version:**
```java
public void foobar() {
    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/data")
            .onResponseSuccess(response -> {
                System.out.println("Request successful, status code: " + response.code());
            })
            .onResponseFailure(response -> {
                switch (response.code()) {
                    case 401:
                        System.out.println("Authentication failed, please login again");
                        break;
                    case 403:
                        System.out.println("Insufficient permissions");
                        break;
                    case 404:
                        System.out.println("Resource not found");
                        break;
                    case 500:
                        System.out.println("Internal server error");
                        break;
                    default:
                        System.out.println("Request failed, status code: " + response.code());
                        break;
                }
            })
            .onSuccess(data -> {
                // Handle successful response data
            });
    
    builder.build().execute();
}
```

## ⚙️ Custom OkHttpClient
### Configuring Timeouts and Connection Pool
**Kotlin Version:**
```kotlin
//import okhttp3.OkHttpClient
//import java.util.concurrent.TimeUnit

// Create custom OkHttpClient
val customClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

// Use custom client
EasyHttpGet.doRequest<ApiResponse>(
    url = "https://api.example.com/large-data",
    okHttpClient = customClient
) { response ->
    // Handle large data response
}
```
**Java Version:**
```java
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public void foobar() {
    // Create custom OkHttpClient
    OkHttpClient customClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    // Use custom client
    EasyHttpGet4j.Builder<ApiResponse> builder = new EasyHttpGet4j.Builder<>(ApiResponse.class)
            .setUrl("https://api.example.com/large-data")
            .setOkHttpClient(customClient)
            .onSuccess(response -> {
                // Handle large data response
            });
    
    builder.build().execute();
}
```

### Adding Interceptors
**Kotlin Version:**
```kotlin
//import okhttp3.Interceptor
//import okhttp3.logging.HttpLoggingInterceptor

// Logging interceptor
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

// Custom header interceptor
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
    // All requests will automatically add version and client type headers
}
```
**Java Version:**
```java
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

public void foobar() {
    // Logging interceptor
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    // Custom header interceptor
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
                // All requests will automatically add version and client type headers
            });
    
    builder.build().execute();
}
```

## 🔄 Custom JSON Serialization
### Configuring Jackson ObjectMapper
**Kotlin Version:**
```kotlin
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.PropertyNamingStrategies
//import com.fasterxml.jackson.module.kotlin.registerKotlinModule

// Custom ObjectMapper
val customMapper = ObjectMapper().apply {
    registerKotlinModule()
    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    // Ignore unknown properties
    configure(com.fasterxml.jackson.databind.DeserializationFeature.
    FAIL_ON_UNKNOWN_PROPERTIES, false)
}

// Use custom serializer
EasyHttpPost.doRequest<User>(
    url = "https://api.example.com/users",
    body = newUser,
    objectMapper = customMapper
) { user ->
    // Handle JSON with snake_case naming strategy
}
```
**Java Version:**
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.DeserializationFeature;

public void foobar() {
    // Custom ObjectMapper
    ObjectMapper customMapper = new ObjectMapper();
    customMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    // Ignore unknown properties
    customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Use custom serializer
    EasyHttpPost4j.Builder<User> builder = new EasyHttpPost4j.Builder<>(User.class)
            .setUrl("https://api.example.com/users")
            .setBody(newUser)
            .setContentType("application/json")
            .setObjectMapper(customMapper)
            .onSuccess(user -> {
                // Handle JSON with snake_case naming strategy
            });
    builder.build().execute();
}
```

## 🧪 Testing and Debugging
### Raw Response Handling
**Kotlin Version:**
```kotlin
// Get raw response for debugging
EasyHttpGet.doRequestRaw(
    url = "https://api.example.com/debug"
) { response ->
    println("Status code: ${response.code}")
    println("Response headers: ${response.headers}")
    println("Response body: ${response.body?.string()}")
}
```
**Java Version:**
```java
public void foobar() {
    // Get raw response for debugging
    EasyHttpGet4j.doRequestRaw(
            "https://api.example.com/debug",
            response -> {
                System.out.println("Status code: " + response.code());
                System.out.println("Response headers: " + response.headers());
                try {
                    if (response.body() != null) {
                        System.out.println("Response body: " + response.body().string());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to read response body: " + e.getMessage());
                }
            }
    );
}
```

### Complex Configuration with Builder Pattern
**Kotlin Version:**
```kotlin
// Use Builder pattern for complex configuration
EasyHttpPost.Builder<ApiResponse>(ApiResponse::class.java)
    .setUrl("https://api.example.com/complex")
    .setBody(complexData)
    .setHeaders(mapOf("Authorization" to "Bearer $token"))
    .setParams(mapOf("version" to "v2"))
    .setCookies(sessionCookies)
    .setOkHttpClient(customClient)
    .setObjectMapper(customMapper)
    .onSuccess { response ->
        println("Success: $response")
    }
    .onException { error, request ->
        println("Error: ${error?.message}")
    }
    .build()
    .execute()
```
**Java Version:**
```java
public void foobar() {
    // Use Builder pattern for complex configuration
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
                System.out.println("Success: " + response);
            })
            .onException((error, request) -> {
                System.out.println("Error: " + (error != null ? error.getMessage() : "unknown error"));
            });
    
    builder.build().execute();
}
```

## 🚀 Best Practices
### 1. Creating API Client Classes
**Kotlin Version:**
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
**Java Version:**
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

### 2. Unified Error Handling
**Kotlin Version:**
```kotlin
object ApiErrorHandler {
    fun handleError(throwable: Throwable?, request: okhttp3.Request) {
        // Unified error handling logic
        when (throwable) {
            is java.net.SocketTimeoutException -> {
                // Show timeout message
            }
            is java.net.UnknownHostException -> {
                // Show network error message
            }
            else -> {
                // Log error
                println("API request failed: ${request.url}, error: ${throwable?.message}")
            }
        }
    }
}

// Use unified error handling in all requests
EasyHttpGet.doRequest<User>(
    url = "https://api.example.com/user",
    exceptionHandler = ApiErrorHandler::handleError
) { user ->
    // Handle user data
}
```
**Java Version:**
```java
public class ApiErrorHandler {
    public static void handleError(Throwable throwable, okhttp3.Request request) {
        // Unified error handling logic
        if (throwable instanceof java.net.SocketTimeoutException) {
            // Show timeout message
            System.out.println("Request timeout");
        } else if (throwable instanceof java.net.UnknownHostException) {
            // Show network error message
            System.out.println("Network connection failed");
        } else {
            // Log error
            System.out.println("API request failed: " + request.url() + ", error: " + 
                (throwable != null ? throwable.getMessage() : "unknown error"));
        }
    }
}

public void foobar() {
    // Use unified error handling in all requests
    EasyHttpGet4j.Builder<User> builder = new EasyHttpGet4j.Builder<>(User.class)
            .setUrl("https://api.example.com/user")
            .onException(ApiErrorHandler::handleError)
            .onSuccess(user -> {
                // Handle user data
            });
    builder.build().execute();
}
```

### 3. Environment Configuration Management
**Kotlin Version:**
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
**Java Version:**
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