package dev.andresoares

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
class NotesApiApplication

fun main(args: Array<String>) {
    runApplication<NotesApiApplication>(*args)
}

