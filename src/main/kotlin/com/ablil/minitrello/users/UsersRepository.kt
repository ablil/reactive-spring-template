package com.ablil.minitrello.users

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UsersRepository: ReactiveCrudRepository<UserDocument, String> {

    fun findByActivationKey(activationKey: String): Mono<UserDocument>
    fun findByPasswordResetKey(key: String): Mono<UserDocument>
    fun findByEmail(email: String): Mono<UserDocument>
}