package dev.andresoares.config

import dev.andresoares.model.Note
import dev.andresoares.model.User
import dev.andresoares.repository.NoteRepository
import dev.andresoares.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer {

    @Bean
    // @Profile("dev")  // Desabilitado para testes via Insomnia
    @Profile("init-data")  // Ativar apenas quando necessário com --spring.profiles.active=dev,init-data
    fun initDatabase(
        noteRepository: NoteRepository,
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ) = CommandLineRunner {
        // Cria um usuário de exemplo para associar às notas
        val sampleUser = userRepository.findByEmail("demo@notesapi.com").orElseGet {
            userRepository.save(
                User(
                    name = "Demo User",
                    email = "demo@notesapi.com",
                    password = passwordEncoder.encode("Demo@123"),
                    roles = mutableSetOf("ROLE_USER")
                )
            )
        }

        // Dados de exemplo para desenvolvimento
        val sampleNotes = listOf(
            Note(
                title = "Bem-vindo à API de Notas",
                content = "Esta é a sua primeira nota! Esta API permite criar, ler, atualizar e excluir notas.",
                user = sampleUser
            ),
            Note(
                title = "Funcionalidades",
                content = """
                    A API oferece as seguintes funcionalidades:
                    - Criar novas notas
                    - Listar todas as notas
                    - Buscar notas por ID
                    - Pesquisar notas por título
                    - Atualizar notas existentes
                    - Excluir notas
                """.trimIndent(),
                user = sampleUser
            ),
            Note(
                title = "Tecnologias Utilizadas",
                content = """
                    - Kotlin 2.2.20
                    - Spring Boot 3.2.2
                    - Spring Data JPA
                    - PostgreSQL
                    - Gradle
                """.trimIndent(),
                user = sampleUser
            ),
            Note(
                title = "Próximos Passos",
                content = """
                    Ideias para expandir o projeto:
                    1. Adicionar autenticação JWT
                    2. Implementar categorias/tags
                    3. Adicionar suporte a anexos
                    4. Criar paginação e ordenação
                    5. Implementar busca full-text
                    6. Adicionar compartilhamento de notas
                """.trimIndent(),
                user = sampleUser
            )
        )

        noteRepository.saveAll(sampleNotes)
        println("✅ Banco de dados inicializado com ${sampleNotes.size} notas de exemplo")
    }
}
