package dev.andresoares.exception

/**
 * Exceção base para erros de negócio
 * HTTP Status: 422 UNPROCESSABLE_ENTITY
 */
open class BusinessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Recurso não encontrado
 * HTTP Status: 404 NOT_FOUND
 */
class ResourceNotFoundException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Recurso já existe (duplicado)
 * HTTP Status: 409 CONFLICT
 */
class ResourceAlreadyExistsException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Operação não permitida por regra de negócio
 * HTTP Status: 422 UNPROCESSABLE_ENTITY
 */
class BusinessRuleViolationException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)

/**
 * Estado inválido para a operação
 * HTTP Status: 409 CONFLICT
 */
class InvalidStateException(
    message: String,
    cause: Throwable? = null
) : BusinessException(message, cause)
