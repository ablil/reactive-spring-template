package com.ablil.minitrello.users

import com.ablil.minitrello.commons.ConflictException
import com.ablil.minitrello.commons.ResourceNotFoundException
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UsersService(val usersRepository: UsersRepository, private val passwordEncoder: PasswordEncoder) {

    fun getAllUsers(pageable: Pageable): Flux<UserDto> = usersRepository
        .findAllBy(pageable)
        .map(UserDocument::toDTO)

    fun findByUsername(username: String) = usersRepository
        .findById(username)
        .map(UserDocument::toDTO)

    fun createUser(request: Mono<CreateUserRequest>): Mono<UserDto> =
        request.flatMap { req ->
            doesUserExist(req.username, req.email)
                .filter { !it }
                .switchIfEmpty(Mono.error(ConflictException("account already exists")))
                .map {
                    UserDocument(
                        username = req.username,
                        email = req.email,
                        password = passwordEncoder.encode(req.password),
                        status = req.status,
                        roles = req.roles,
                    )
                }.flatMap { usersRepository.save(it) }
                .map(UserDocument::toDTO)

        }

    private fun doesUserExist(username: String, email: String): Mono<Boolean> =
        Mono.zip(
            usersRepository.existsByEmail(email),
            usersRepository.existsById(username)
        ).map { it.t1 || it.t2 }

    fun updateUser(username: String, request: Mono<UpdateUserRequest>): Mono<UserDto> =
        usersRepository.findById(username)
            .flatMap { user ->
                request.map {
                    user.copy(
                        email = it.email,
                        password = passwordEncoder.encode(it.password),
                        roles = it.roles,
                        status = it.status
                    )
                }
            }
            .flatMap { usersRepository.save(it) }
            .map(UserDocument::toDTO)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("account does not exist")))

    fun deleteUser(username: String) = usersRepository.deleteById(username)

}