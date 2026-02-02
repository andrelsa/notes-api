package dev.andresoares.exception

/**
 * Exceção base para erros de segurança
 * HTTP Status: 401 UNAUTHORIZED ou 403 FORBIDDEN
 */
open class SecurityException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Credenciais inválidas ou ausentes
 * HTTP Status: 401 UNAUTHORIZED
 */
class UnauthorizedException(
    message: String = "Authentication is required to access this resource",
    cause: Throwable? = null
) : SecurityException(message, cause)

/**
 * Token inválido ou expirado
 * HTTP Status: 401 UNAUTHORIZED
 */
class InvalidTokenException(
    message: String = "Invalid or expired authentication token",
    cause: Throwable? = null
) : SecurityException(message, cause)

/**
 * Acesso negado por falta de permissão
 * HTTP Status: 403 FORBIDDEN
 */
class AccessDeniedException(
    message: String = "You don't have permission to access this resource",
    cause: Throwable? = null
) : SecurityException(message, cause)

/**
 * Recurso ou operação requer autenticação
 * HTTP Status: 401 UNAUTHORIZED
 */
class AuthenticationRequiredException(
    message: String = "Authentication is required",
    cause: Throwable? = null
) : SecurityException(message, cause)
