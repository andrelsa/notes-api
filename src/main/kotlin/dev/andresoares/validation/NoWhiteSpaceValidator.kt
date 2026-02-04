package dev.andresoares.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NoWhiteSpaceValidator : ConstraintValidator<NoWhitespace, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true // Constraint validation should not be applied to null values.
        }

        // Verifica se contém qualquer espaço em branco
        return !value.contains(Regex("\\s"))
    }
}
