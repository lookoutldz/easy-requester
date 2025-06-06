package io.github.lookoutldz.easyrequester

/**
 *  @author looko
 *  @date 2025/5/26
 *
 */

data class ResponseBody<T>(
    var data: T,
    var statusCode: Int,
    var statusMessage: String,
)

data class User(val userId: Long, val name: String)

