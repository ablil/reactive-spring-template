package com.ablil.minitrello.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(web: ServerHttpSecurity): SecurityWebFilterChain {
        return web.invoke {
            authorizeExchange {
                authorize("/actuator/health", permitAll)
                authorize("/api/v1/signup", permitAll)
                authorize("/api/v1/signin", permitAll)
                authorize("/api/v1/activate", permitAll)
                authorize("/api/v1/requestpasswordreset", permitAll)
                authorize("/api/v1/resetpassword", permitAll)

                authorize("/swagger-ui/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/v3/api-docs.yaml", permitAll)

                authorize(anyExchange, authenticated)
            }
            formLogin { disable() }
            cors { disable() }
            csrf { disable() }
//            httpBasic { }
            oauth2ResourceServer { jwt { } }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    //    @Bean
    fun userDetailsService(): ReactiveUserDetailsService = MapReactiveUserDetailsService(
        User.builder()
            .username("admin")
            .password("supersecurepassword")
            .accountLocked(false)
            .accountExpired(false)
            .credentialsExpired(false)
            .build()
    )
}