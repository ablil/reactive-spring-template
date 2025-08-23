package com.ablil.minitrello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
class MainApplication

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}
