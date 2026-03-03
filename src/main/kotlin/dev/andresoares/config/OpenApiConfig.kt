package dev.andresoares.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuração do Swagger / OpenAPI 3.
 *
 * Acessível em:
 * - Swagger UI:  http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/api-docs
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("Notes API")
                    .description(
                        """
                        API REST para gerenciamento de notas com autenticação JWT e controle de acesso por roles.
                        
                        ## Autenticação
                        1. Cadastre-se via `POST /api/v1/users`
                        2. Faça login via `POST /api/v1/auth/login` para obter o `accessToken`
                        3. Clique em **Authorize** e informe: `Bearer {accessToken}`
                        
                        ## Roles disponíveis
                        | Role | Permissões |
                        |------|-----------|
                        | `ROLE_USER` | Gerencia suas próprias notas |
                        | `ROLE_VIEWER` | Leitura das próprias notas |
                        | `ROLE_MANAGER` | Visualiza todas as notas; edita/deleta as próprias |
                        | `ROLE_ADMIN` | Acesso total ao sistema |
                        """.trimIndent()
                    )
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("André Soares")
                            .url("https://github.com/andrelsa/notes-api")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Informe o JWT obtido em POST /api/v1/auth/login")
                )
            )
    }
}

