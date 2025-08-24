package com.ablil.minitrello.accounts

import com.ablil.minitrello.security.JwtUtils
import com.ablil.minitrello.security.SecurityUtils
import com.ablil.minitrello.users.AccountStatus
import com.ablil.minitrello.users.UserDocument
import com.ablil.minitrello.users.UserRole
import com.ablil.minitrello.users.UsersRepository
import io.micrometer.observation.annotation.Observed
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class AccountService(
    val usersRepository: UsersRepository,
    val passwordEncoder: PasswordEncoder,
    val jwtEncoder: JwtEncoder
) {

    fun registerNewAccount(request: Mono<SignUpRequest>): Mono<UserDocument> {
        return request
            .filterWhen { usersRepository.existsById(it.username).map(Boolean::not) }
            .map {
                UserDocument(
                    username = it.username,
                    password = passwordEncoder.encode(it.password),
                    status = AccountStatus.INACTIVE,
                    email = it.email,
                    roles = setOf(),
                    activationKey = generateRandomKey()
                )
            }
            .flatMap { usersRepository.save(it) }
    }

    fun authenticateAccount(request: Mono<SignInRequest>): Mono<String> {
        return request
            .flatMap { req ->
                usersRepository.findById(req.username)
                    .filter { passwordEncoder.matches(req.password, it.password) }
                    .filter { it.status == AccountStatus.ACTIVE }
            }
            .flatMap { JwtUtils.generateToken(it, jwtEncoder) }
    }

    fun activateAccount(key: String): Mono<Void> {
        return usersRepository.findByActivationKey(key)
            .map { it.copy(status = AccountStatus.ACTIVE, activationKey = null) }
            .flatMap { usersRepository.save(it) }
            .switchIfEmpty(Mono.error(IllegalArgumentException("No activation key found")))
            .then()
    }


    @Transactional(readOnly = true)
    fun getAuthenticatedUser(): Mono<AccountDto> {
        return SecurityUtils.getAuthenticatedUsername()
            .flatMap(usersRepository::findById)
            .filter(UserDocument::isActive)
            .map {
                AccountDto(
                    username = it.username,
                    email = it.email,
                    roles = it.roles.map(UserRole::name).toSet(),
                )
            }
            .switchIfEmpty(Mono.error(IllegalArgumentException("security context was empty or user not active")))
    }

    fun requestPasswordReset(@Email email: String): Mono<Void> {
        return usersRepository.findByEmail(email)
            .map { it.copy(passwordResetKey = generateRandomKey()) }
            .flatMap { usersRepository.save(it) }
            .then()
    }

    fun resetPassword(request: Mono<ResetPasswordRequest>): Mono<Void> {
        return request.flatMap { req ->
            usersRepository.findByPasswordResetKey(req.key)
                .switchIfEmpty(Mono.error(IllegalArgumentException("invalid key")))
                .filter { document -> !passwordEncoder.matches(req.newPassword, document.password) }
                .switchIfEmpty(Mono.error(IllegalStateException("can NOT use the same password")))
                .map { document ->
                    document.copy(
                        password = passwordEncoder.encode(req.newPassword),
                        passwordResetKey = null
                    )
                }
                .flatMap { usersRepository.save(it) }
                .then()
        }
    }

    fun updatePassword(@Valid request: Mono<UpdatePasswordRequest>): Mono<Void> {
        return request
            .filter { it.oldPassword != it.newPassword }
            .switchIfEmpty(Mono.error(IllegalArgumentException("can NOT reuse the same password")))
            .flatMap { req ->
                SecurityUtils.getAuthenticatedUsername()
                    .flatMap { usersRepository.findById(it) }
                    .filter { passwordEncoder.matches(req.oldPassword, it.password) }
                    .switchIfEmpty(Mono.error(IllegalArgumentException("invalid old password")))
                    .map { document -> document.copy(password = passwordEncoder.encode(req.newPassword)) }
                    .flatMap { usersRepository.save(it) }
                    .then()
            }
    }
}

fun generateRandomKey() = RandomStringUtils.secure().nextAlphanumeric(20)