package com.ablil.minitrello.users

import com.ablil.minitrello.PersistenceTestConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier


@SpringBootTest(classes = [PersistenceTestConfiguration::class])
@AutoConfigureWebTestClient
@WithMockUser("johndoe", roles = ["ADMIN"])
class UsersControllerTest @Autowired constructor(
    val client: WebTestClient,
    val objectMapper: ObjectMapper,
    val usersRepository: UsersRepository,
    val passwordEncoder: PasswordEncoder
) {

    @BeforeEach
    fun setup() {
        usersRepository.deleteAll().block()
        usersRepository.save(
            UserDocument(
                username = "johndoe",
                email = "johndoe@example.com",
                password = "{noop}supersecurepassword",
                status = AccountStatus.ACTIVE,
                roles = emptySet()
            )
        ).block()
    }

    @Test
    fun `get user by username`() {
        client.get()
            .uri("/api/v1/users/johndoe")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").value(IsEqual("johndoe"))
            .jsonPath("$.email").value(IsEqual("johndoe@example.com"))
            .jsonPath("$.roles").isEmpty
            .jsonPath("$.status").value(IsEqual("ACTIVE"))
    }

    @Test
    fun `create user`() {
        client.post()
            .uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                objectMapper.writeValueAsString(
                    CreateUserRequest(
                        username = "janedoe",
                        email = "janedoe@example.com",
                        password = "supersecurepassword",
                        roles = setOf(UserRole.MANAGER),
                        status = AccountStatus.INACTIVE
                    )
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.username").value(IsEqual("janedoe"))
            .jsonPath("$.email").value(IsEqual("janedoe@example.com"))
            .jsonPath("$.status").value(IsEqual("INACTIVE"))
            .jsonPath("$.roles").value<List<String>> { assertThat(it).contains("MANAGER") }


        StepVerifier.create(usersRepository.findById("janedoe"))
            .assertNext {
                assertThat(it.email).isEqualTo("janedoe@example.com")
                assertThat(passwordEncoder.matches("supersecurepassword", it.password)).isTrue
                assertThat(it.roles).contains(UserRole.MANAGER)
                assertThat(it.status).isEqualTo(AccountStatus.INACTIVE)
            }
            .verifyComplete()
    }

    @Test
    fun `update user`() {

        client.put()
            .uri("/api/v1/users/johndoe")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                objectMapper.writeValueAsString(
                    UpdateUserRequest(
                        email = "newemail@example.com",
                        password = "newpassword",
                        roles = setOf(UserRole.ADMIN),
                        status = AccountStatus.INACTIVE
                    )
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").value(IsEqual("newemail@example.com"))
            .jsonPath("$.status").value(IsEqual("INACTIVE"))
            .jsonPath("$.roles").value<List<String>> { assertThat(it).contains("ADMIN") }

        StepVerifier.create(usersRepository.findById("johndoe"))
            .assertNext {
                assertThat(it.email).isEqualTo("newemail@example.com")
                assertThat(it.status).isEqualTo(AccountStatus.INACTIVE)
                assertThat(it.roles).contains(UserRole.ADMIN)
                assertThat(passwordEncoder.matches("newpassword", it.password)).isTrue
            }
            .verifyComplete()
    }

    @Test
    fun `delete user`() {
        client.delete()
            .uri("/api/v1/users/johndoe")
            .exchange()
            .expectStatus().isNoContent

        StepVerifier.create(usersRepository.existsById("johndoe"))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `get all users paginated`() {
        client.get()
            .uri("/api/v1/users")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.page").value(IsEqual(0))
            .jsonPath("$.size").value(IsEqual(100))
            .jsonPath("$.items.length()").value<Int> { assertThat(it).isGreaterThan(0) }
            .jsonPath("$.items[*].username").value<List<String>> { assertThat(it).contains("johndoe") }

        client.get()
            .uri("/api/v1/users?page=99")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.page").value(IsEqual(99))
            .jsonPath("$.items.length()").value(IsEqual(0))
    }
}