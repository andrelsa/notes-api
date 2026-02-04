package dev.andresoares.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordValidator::class])
annotation class ValidPassword(
    val message: String = "Password must contain at least one letter, one number, and one special character",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
