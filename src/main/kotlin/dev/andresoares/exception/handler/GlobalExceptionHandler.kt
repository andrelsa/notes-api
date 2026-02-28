package dev.andresoares.exception.handler

import dev.andresoares.exception.*
import dev.andresoares.exception.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.access.AccessDeniedException as SpringAccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.*

/**
 * Handler global de exceções da aplicação
 * Captura e trata todas as exceções, padronizando as respostas em JSON
 * e facilitando a observabilidade através de logs estruturados
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // ==================== EXCEÇÕES DE NEGÓCIO ====================

    /**
     * Trata exceções de recurso não encontrado
     * HTTP Status: 404 NOT_FOUND
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.reasonPhrase)
            .message(ex.message ?: "Resource not found")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Trata exceções de recurso já existente (conflito)
     * HTTP Status: 409 CONFLICT
     */
    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(
        ex: ResourceAlreadyExistsException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource conflict: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .error(HttpStatus.CONFLICT.reasonPhrase)
            .message(ex.message ?: "Resource already exists")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    /**
     * Trata exceções de violação de regra de negócio
     * HTTP Status: 422 UNPROCESSABLE_ENTITY
     */
    @ExceptionHandler(BusinessRuleViolationException::class, InvalidStateException::class)
    fun handleBusinessRuleViolationException(
        ex: BusinessException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Business rule violation: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error(HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase)
            .message(ex.message ?: "Business rule violation")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    /**
     * Trata exceções de role inválida
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(InvalidRoleException::class)
    fun handleInvalidRoleException(
        ex: InvalidRoleException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid role: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.reasonPhrase)
            .message(ex.message ?: "Invalid role")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Trata exceções genéricas de negócio
     * HTTP Status: 422 UNPROCESSABLE_ENTITY
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Business exception: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error(HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase)
            .message(ex.message ?: "Business error occurred")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    // ==================== EXCEÇÕES DE VALIDAÇÃO ====================

    /**
     * Trata erros de validação do Bean Validation (@Valid)
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation error: ${ex.message}")

        val validationErrors = ex.bindingResult.allErrors
            .groupBy { (it as? FieldError)?.field ?: "general" }
            .mapValues { (_, errors) ->
                errors.map { it.defaultMessage ?: "Invalid value" }
            }

        // Criar mensagem mais específica baseada nos erros
        val message = when {
            validationErrors.size == 1 -> {
                val field = validationErrors.keys.first()
                val error = validationErrors[field]?.first() ?: "Validation failed"
                if (error.contains("required") || error.contains("must be provided")) {
                    "Required field '$field' is missing or invalid. $error"
                } else {
                    "Validation failed for field '$field': $error"
                }
            }
            validationErrors.isNotEmpty() -> {
                val requiredFields = validationErrors.filter { (_, errors) ->
                    errors.any { it.contains("required") || it.contains("must be provided") }
                }
                if (requiredFields.isNotEmpty()) {
                    "Required fields are missing or invalid: ${requiredFields.keys.joinToString(", ")}"
                } else {
                    "Validation failed for ${validationErrors.size} field(s)"
                }
            }
            else -> "Validation failed for one or more fields"
        }

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.reasonPhrase)
            .message(message)
            .path(request.requestURI)
            .traceId(generateTraceId())
            .validationErrors(validationErrors)
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Trata exceções customizadas de validação
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(ValidationException::class, InvalidInputException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation exception: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.reasonPhrase)
            .message(ex.message ?: "Validation error")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .validationErrors(ex.errors.takeIf { it.isNotEmpty() })
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Trata parâmetros ausentes
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Missing parameter: ${ex.parameterName}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.reasonPhrase)
            .message("Required parameter '${ex.parameterName}' is missing")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .addDetail("parameterName", ex.parameterName)
            .addDetail("parameterType", ex.parameterType)
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Trata erro de tipo de argumento incorreto
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Type mismatch for parameter: ${ex.name}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.reasonPhrase)
            .message("Invalid value for parameter '${ex.name}'. Expected type: ${ex.requiredType?.simpleName}")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .addDetail("parameterName", ex.name)
            .addDetail("rejectedValue", ex.value ?: "null")
            .addDetail("requiredType", ex.requiredType?.simpleName ?: "unknown")
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Trata erro de JSON mal formatado
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed JSON request: ${ex.message}")

        // Tentar extrair informações mais específicas do erro
        val rootCause = ex.rootCause
        var detailedMessage = "Malformed JSON request. Please check your request body"
        val details = mutableMapOf<String, Any>()

        // Verificar se é um problema de campo ausente ou null
        val exceptionMessage = ex.message ?: ""
        when {
            exceptionMessage.contains("Required request body is missing") -> {
                detailedMessage = "Request body is required. Please provide a valid JSON body"
            }
            exceptionMessage.contains("JSON parse error") -> {
                detailedMessage = "Invalid JSON format. Please check the syntax of your JSON request"
                // Tentar extrair qual campo causou o problema
                if (rootCause != null) {
                    val causeMessage = rootCause.message ?: ""
                    if (causeMessage.contains("Cannot deserialize value")) {
                        detailedMessage = "Invalid value format in JSON request"
                      }
                }
            }
            exceptionMessage.contains("not present") || exceptionMessage.contains("missing") -> {
                detailedMessage = "Required fields are missing in the request body. Please ensure all mandatory fields are provided"
                details["hint"] = "Check that 'title' field is present and not null"
            }
        }

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.reasonPhrase)
            .message(detailedMessage)
            .path(request.requestURI)
            .traceId(generateTraceId())
            .apply {
                if (details.isNotEmpty()) {
                    details(details)
                }
            }
            .build()

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // ==================== EXCEÇÕES DE INFRAESTRUTURA ====================

    /**
     * Trata exceções de acesso a dados (banco de dados)
     * HTTP Status: 503 SERVICE_UNAVAILABLE ou 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(
        ex: DataAccessException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Database access error: ${ex.message}", ex)

        val status = when (ex) {
            is DataIntegrityViolationException -> HttpStatus.CONFLICT
            else -> HttpStatus.SERVICE_UNAVAILABLE
        }

        val errorResponse = ErrorResponse.builder()
            .status(status.value())
            .error(status.reasonPhrase)
            .message("Database operation failed. Please try again later")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(status).body(errorResponse)
    }

    /**
     * Trata exceções de serviço externo indisponível
     * HTTP Status: 503 SERVICE_UNAVAILABLE
     */
    @ExceptionHandler(ExternalServiceUnavailableException::class)
    fun handleExternalServiceUnavailableException(
        ex: ExternalServiceUnavailableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("External service unavailable: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error(HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase)
            .message(ex.message ?: "External service is unavailable")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)
    }

    /**
     * Trata exceções de timeout
     * HTTP Status: 504 GATEWAY_TIMEOUT
     */
    @ExceptionHandler(OperationTimeoutException::class)
    fun handleOperationTimeoutException(
        ex: OperationTimeoutException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Operation timeout: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.GATEWAY_TIMEOUT.value())
            .error(HttpStatus.GATEWAY_TIMEOUT.reasonPhrase)
            .message(ex.message ?: "Operation timed out")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse)
    }

    /**
     * Trata exceções genéricas de infraestrutura
     * HTTP Status: 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(InfrastructureException::class)
    fun handleInfrastructureException(
        ex: InfrastructureException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Infrastructure error: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase)
            .message("An infrastructure error occurred. Please try again later")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    // ==================== EXCEÇÕES HTTP ====================

    /**
     * Trata erro de método HTTP não suportado
     * HTTP Status: 405 METHOD_NOT_ALLOWED
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Method not allowed: ${ex.method} for ${request.requestURI}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.METHOD_NOT_ALLOWED.value())
            .error(HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase)
            .message("HTTP method '${ex.method}' is not supported for this endpoint")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .addDetail("supportedMethods", ex.supportedHttpMethods ?: emptySet<String>())
            .build()

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse)
    }

    /**
     * Trata erro de media type não suportado
     * HTTP Status: 415 UNSUPPORTED_MEDIA_TYPE
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Unsupported media type: ${ex.contentType}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
            .error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.reasonPhrase)
            .message("Media type '${ex.contentType}' is not supported")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .addDetail("supportedMediaTypes", ex.supportedMediaTypes.map { it.toString() })
            .build()

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse)
    }

    /**
     * Trata erro de endpoint não encontrado
     * HTTP Status: 404 NOT_FOUND
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("No handler found for: ${ex.requestURL}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.reasonPhrase)
            .message("No endpoint found for ${ex.httpMethod} ${ex.requestURL}")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    // ==================== EXCEÇÕES DE SEGURANÇA ====================

    /**
     * Trata exceções de credenciais inválidas (Spring Security)
     * HTTP Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler(BadCredentialsException::class, AuthenticationException::class)
    fun handleBadCredentialsException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Authentication failed: ${ex.message}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .error(HttpStatus.UNAUTHORIZED.reasonPhrase)
            .message(ex.message ?: "Invalid credentials")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * Trata exceções de não autorizado
     * HTTP Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler(
        UnauthorizedException::class,
        InvalidTokenException::class,
        AuthenticationRequiredException::class
    )
    fun handleUnauthorizedException(
        ex: SecurityException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Unauthorized access attempt: ${ex.message}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .error(HttpStatus.UNAUTHORIZED.reasonPhrase)
            .message(ex.message ?: "Authentication is required")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * Trata exceções de acesso negado lançadas pelo Spring Security (@PreAuthorize, hasRole, etc.)
     * HTTP Status: 403 FORBIDDEN
     */
    @ExceptionHandler(SpringAccessDeniedException::class)
    fun handleSpringAccessDeniedException(
        ex: SpringAccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied by Spring Security: ${ex.message}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .error(HttpStatus.FORBIDDEN.reasonPhrase)
            .message("You don't have permission to access this resource")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    /**
     * Trata exceções de acesso negado
     * HTTP Status: 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: ${ex.message}")

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .error(HttpStatus.FORBIDDEN.reasonPhrase)
            .message(ex.message ?: "Access denied")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .build()

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    // ==================== EXCEÇÃO GENÉRICA ====================

    /**
     * Captura todas as exceções não tratadas especificamente
     * HTTP Status: 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred: ${ex.message}", ex)

        val errorResponse = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase)
            .message("An unexpected error occurred. Please contact support if the problem persists")
            .path(request.requestURI)
            .traceId(generateTraceId())
            .addDetail("exceptionType", ex.javaClass.simpleName)
            .build()

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Gera um ID de rastreamento único para cada requisição
     * Útil para correlacionar logs e debugging
     */
    private fun generateTraceId(): String {
        return UUID.randomUUID().toString()
    }
}
