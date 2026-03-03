package dev.andresoares

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class NotesApiApplication

fun main(args: Array<String>) {
    runApplication<NotesApiApplication>(*args)
}
