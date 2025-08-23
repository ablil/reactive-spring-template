package com.ablil.minitrello

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class HomeController {

    @GetMapping("/home")
    fun home(): Mono<String> = Mono.just<String>("It works!!")
}