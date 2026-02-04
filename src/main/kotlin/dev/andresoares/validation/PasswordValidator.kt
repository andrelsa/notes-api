package dev.andresoares.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PasswordValidator : ConstraintValidator<ValidPassword, String> {

    override fun isValid(password: String?, context: ConstraintValidatorContext): Boolean {
        if (password.isNullOrBlank()) {
            return true // @NotBlank já valida isso
        }

        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasLetter && hasDigit && hasSpecialChar
    }
}
