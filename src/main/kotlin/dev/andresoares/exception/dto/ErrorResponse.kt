package dev.andresoares.exception.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * Resposta padrão de erro da API
 * Fornece informações detalhadas sobre o erro para debugging e observabilidade
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val traceId: String? = null,
    val details: Map<String, Any>? = null,
    val validationErrors: Map<String, List<String>>? = null
) {
    companion object {
        fun builder() = ErrorResponseBuilder()
    }
}

/**
 * Builder para facilitar a construção de ErrorResponse
 */
class ErrorResponseBuilder {
    private var timestamp: LocalDateTime = LocalDateTime.now()
    private var status: Int = 500
    private var error: String = "Internal Server Error"
    private var message: String = "An unexpected error occurred"
    private var path: String? = null
    private var traceId: String? = null
    private var details: MutableMap<String, Any>? = null
    private var validationErrors: Map<String, List<String>>? = null

    fun timestamp(timestamp: LocalDateTime) = apply { this.timestamp = timestamp }
    fun status(status: Int) = apply { this.status = status }
    fun error(error: String) = apply { this.error = error }
    fun message(message: String) = apply { this.message = message }
    fun path(path: String?) = apply { this.path = path }
    fun traceId(traceId: String?) = apply { this.traceId = traceId }
    fun validationErrors(errors: Map<String, List<String>>?) = apply { this.validationErrors = errors }

    fun addDetail(key: String, value: Any) = apply {
        if (details == null) {
            details = mutableMapOf()
        }
        details!![key] = value
    }

    fun details(details: Map<String, Any>?) = apply {
        this.details = details?.toMutableMap()
    }

    fun build() = ErrorResponse(
        timestamp = timestamp,
        status = status,
        error = error,
        message = message,
        path = path,
        traceId = traceId,
        details = details,
        validationErrors = validationErrors
    )
}
