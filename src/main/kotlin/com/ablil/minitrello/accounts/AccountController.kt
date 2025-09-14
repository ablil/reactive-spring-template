package com.ablil.minitrello.accounts

import com.ablil.minitrello.commons.ConflictException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1")
class AccountController(val accountService: AccountService) {

    @PostMapping("signup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun signUp(@RequestBody body: Mono<SignUpRequest>): Mono<Void> =
        accountService
            .registerNewAccount(body)
            .switchIfEmpty(Mono.error(ConflictException("account already exists")))
            .then()


    @PostMapping("signin")
    fun signIn(@RequestBody request: Mono<SignInRequest>): Mono<TokenDto> =
        accountService
            .authenticateAccount(request)
            .map { TokenDto(it) }
            .switchIfEmpty(Mono.error(BadCredentialsException("bad credentials")))


    @GetMapping("activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun activateAccount(@RequestParam("key") key: String): Mono<Void> =
        accountService.activateAccount(key).then()

    @GetMapping("/accounts/current")
    @Operation(security = [SecurityRequirement(name = "bearer")])
    fun getAuthenticatedUser(): Mono<AccountDto> {
        return accountService.getAuthenticatedUser()
    }
}

data class SignUpRequest(
    @NotBlank val username: String,
    @Email val email: String,
    @NotBlank val password: String,
)

data class SignInRequest(
    val username: String,
    val password: String
)

data class TokenDto(val token: String)

data class AccountDto(
    val username: String,
    val email: String,
    val roles: Set<String>
)