package me.cyber.nukleos.api

import com.google.gson.JsonElement
import java.util.*

/**
 * Base classes for requests and responses
 */

data class ApiError(val message: String = "", val code: Int)
data class ApiResponse(val id: String, val method: String, val error: ApiError? = null, val data: JsonElement?)