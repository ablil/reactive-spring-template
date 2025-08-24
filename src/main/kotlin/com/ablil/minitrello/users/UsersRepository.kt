package com.ablil.minitrello.users

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.data.repository.reactive.ReactiveSortingRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UsersRepository : ReactiveCrudRepository<UserDocument, String>,
    ReactiveSortingRepository<UserDocument, String> {

    fun findByActivationKey(activationKey: String): Mono<UserDocument>
    fun findByPasswordResetKey(key: String): Mono<UserDocument>
    fun findByEmail(email: String): Mono<UserDocument>

    fun existsByEmail(email: String): Mono<Boolean>

    fun findAllBy(pageable: Pageable): Flux<UserDocument>
}