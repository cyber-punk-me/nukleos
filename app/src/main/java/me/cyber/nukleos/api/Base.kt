package me.cyber.nukleos.api

import com.google.gson.JsonElement

/**
 * Base classes for requests and responses
 */

data class ApiError(val message: String = "", val code: Int)
data class ApiRequest(val id: String, val method: String, val auth: String? = null)
data class ApiResponse(val id: String, val method: String, val error: ApiError? = null, val data: JsonElement?)