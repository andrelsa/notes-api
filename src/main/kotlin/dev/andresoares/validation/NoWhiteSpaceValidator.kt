package dev.andresoares.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NoWhiteSpaceValidator : ConstraintValidator<NoWhitespace, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) {
            return true // @NotBlank já valida isso
        }

        // Verifica se contém qualquer espaço em branco
        return !value.contains(Regex("\\s"))
    }
}
