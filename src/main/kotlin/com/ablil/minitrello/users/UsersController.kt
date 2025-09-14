package com.ablil.minitrello.users

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer")
class UsersController(val usersService: UsersService) {

    @GetMapping
    fun getAllUsers(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "100") size: Int
    ): Mono<PaginatedResponse<UserDto>> =
        usersService.getAllUsers(PageRequest.of(page, size))
            .collectList()
            .map { PaginatedResponse(page.toLong(), size.toLong(), it) }


    @GetMapping("{username}")
    fun getUserByUsername(@PathVariable("username") username: String): Mono<UserDto> =
        usersService.findByUsername(username)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody body: Mono<CreateUserRequest>): Mono<UserDto> = usersService.createUser(body)

    @PutMapping("{username}")
    fun updateUser(
        @PathVariable("username") username: String,
        @RequestBody body: Mono<UpdateUserRequest>
    ): Mono<UserDto> = usersService.updateUser(username, body)

    @DeleteMapping("{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable("username") username: String): Mono<Void> =
        usersService.deleteUser(username).then()
}

data class UserDto(
    val username: String,
    val email: String,
    val roles: Set<String>,
    val status: AccountStatus
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val roles: Set<UserRole>,
    val status: AccountStatus,
)

data class UpdateUserRequest(
    val email: String,
    val password: String,
    val roles: Set<UserRole>,
    val status: AccountStatus
)


data class PaginatedResponse<T>(
    val page: Long,
    val size: Long,
    val items: List<T>
)