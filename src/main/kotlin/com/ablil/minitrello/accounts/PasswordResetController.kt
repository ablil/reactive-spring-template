package com.ablil.minitrello.accounts

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Email
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1")
class PasswordResetController(val accountService: AccountService) {

    @PostMapping("requestpasswordreset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun requestPasswordReset(@RequestBody body: Mono<EmailWrapper>): Mono<Void> =
        body.flatMap { accountService.requestPasswordReset(it.email) }

    @PostMapping("resetpassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resetPassword(@RequestBody body: Mono<ResetPasswordRequest>): Mono<Void> =
        accountService.resetPassword(body)

    @PutMapping("updatepassword")
    @Operation(security = [SecurityRequirement(name = "bearer")])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updatePassword(@RequestBody body: Mono<UpdatePasswordRequest>): Mono<Void> =
        accountService.updatePassword(body)
}

data class EmailWrapper(@Email val email: String)

data class ResetPasswordRequest(val key: String, val newPassword: String)

data class UpdatePasswordRequest(val oldPassword: String, val newPassword: String)