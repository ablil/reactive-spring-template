package com.ablil.minitrello.security

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Mono

object SecurityUtils {

    fun getAuthenticatedUsername(): Mono<String> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal }
            .mapNotNull {
                when (it) {
                    is Jwt -> it.subject
                    is User -> it.username
                    else -> error("unknown authentication principal")
                }
            }
    }
}