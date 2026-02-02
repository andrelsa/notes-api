package dev.andresoares.config

import dev.andresoares.model.Note
import dev.andresoares.repository.NoteRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DataInitializer {

    @Bean
    // @Profile("dev")  // Desabilitado para testes via Insomnia
    @Profile("init-data")  // Ativar apenas quando necessário com --spring.profiles.active=dev,init-data
    fun initDatabase(noteRepository: NoteRepository) = CommandLineRunner {
        // Dados de exemplo para desenvolvimento
        val sampleNotes = listOf(
            Note(
                title = "Bem-vindo à API de Notas",
                content = "Esta é a sua primeira nota! Esta API permite criar, ler, atualizar e excluir notas."
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
                """.trimIndent()
            ),
            Note(
                title = "Tecnologias Utilizadas",
                content = """
                    - Kotlin 2.2.20
                    - Spring Boot 3.2.2
                    - Spring Data JPA
                    - H2 Database (em memória)
                    - Gradle
                """.trimIndent()
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
                """.trimIndent()
            )
        )

        noteRepository.saveAll(sampleNotes)
        println("✅ Banco de dados inicializado com ${sampleNotes.size} notas de exemplo")
    }
}
