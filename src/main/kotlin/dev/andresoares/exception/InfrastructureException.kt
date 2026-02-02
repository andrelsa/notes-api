package dev.andresoares.exception

/**
 * Exceção base para erros de infraestrutura
 * HTTP Status: 500 INTERNAL_SERVER_ERROR ou 503 SERVICE_UNAVAILABLE
 */
open class InfrastructureException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Erro de conexão com banco de dados
 * HTTP Status: 503 SERVICE_UNAVAILABLE
 */
class DatabaseConnectionException(
    message: String,
    cause: Throwable? = null
) : InfrastructureException(message, cause)

/**
 * Erro ao executar operação no banco de dados
 * HTTP Status: 500 INTERNAL_SERVER_ERROR
 */
class DatabaseOperationException(
    message: String,
    cause: Throwable? = null
) : InfrastructureException(message, cause)

/**
 * Serviço externo indisponível
 * HTTP Status: 503 SERVICE_UNAVAILABLE
 */
class ExternalServiceUnavailableException(
    serviceName: String,
    cause: Throwable? = null
) : InfrastructureException("External service '$serviceName' is unavailable", cause)

/**
 * Timeout em operação
 * HTTP Status: 504 GATEWAY_TIMEOUT
 */
class OperationTimeoutException(
    message: String,
    cause: Throwable? = null
) : InfrastructureException(message, cause)
