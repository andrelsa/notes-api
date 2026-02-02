package dev.andresoares.exception

/**
 * Exceção base para erros de validação
 * HTTP Status: 400 BAD_REQUEST
 */
open class ValidationException(
    message: String,
    val errors: Map<String, List<String>> = emptyMap(),
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Erro de validação de dados de entrada
 * HTTP Status: 400 BAD_REQUEST
 */
class InvalidInputException(
    message: String,
    errors: Map<String, List<String>> = emptyMap(),
    cause: Throwable? = null
) : ValidationException(message, errors, cause)

/**
 * Parâmetro obrigatório ausente
 * HTTP Status: 400 BAD_REQUEST
 */
class MissingParameterException(
    parameterName: String,
    cause: Throwable? = null
) : ValidationException("Required parameter '$parameterName' is missing", emptyMap(), cause)

/**
 * Formato de dados inválido
 * HTTP Status: 400 BAD_REQUEST
 */
class InvalidFormatException(
    message: String,
    cause: Throwable? = null
) : ValidationException(message, emptyMap(), cause)
