package com.ablil.minitrello.accounts

import com.ablil.minitrello.PersistenceTestConfiguration
import com.ablil.minitrello.users.AccountStatus
import com.ablil.minitrello.users.UserDocument
import com.ablil.minitrello.users.UsersRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.text.IsEqualIgnoringCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier

@SpringBootTest(classes = [PersistenceTestConfiguration::class])
@AutoConfigureWebTestClient
class AccountControllerTest @Autowired constructor(
    val client: WebTestClient,
    val objectMapper: ObjectMapper,
    val usersRepository: UsersRepository
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
                roles = emptySet(),
                activationKey = null,
                passwordResetKey = null
            )
        ).block()
    }


    @Test
    fun `create new account`() {
        client.post().uri("/api/v1/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                objectMapper.writeValueAsString(
                    SignUpRequest(
                        username = "janedoe",
                        email = "janedoe@example.com",
                        password = "supersecurepassword"
                    )
                )
            )
            .exchange()
            .expectStatus().isNoContent


        StepVerifier.create(usersRepository.findById("janedoe"))
            .assertNext {
                assertThat(it.status).isEqualTo(AccountStatus.INACTIVE)
                assertThat(it.activationKey).isNotBlank
            }
            .verifyComplete()
    }

    @Test
    fun `sign in user with username and password`() {
        client.post()
            .uri("/api/v1/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                objectMapper.writeValueAsString(
                    SignInRequest(
                        username = "johndoe",
                        password = "supersecurepassword"
                    )
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.token").isNotEmpty
    }

    @Test
    fun `activate user account`() {
        usersRepository.findById("johndoe")
            .map { it.copy(status = AccountStatus.INACTIVE, activationKey = "randomkey") }
            .flatMap { usersRepository.save(it) }
            .block()

        client.get()
            .uri("/api/v1/activate?key=randomkey")
            .exchange()
            .expectStatus().isNoContent

        StepVerifier.create(usersRepository.findById("johndoe"))
            .assertNext {
                assertThat(it.status).isEqualTo(AccountStatus.ACTIVE)
                assertThat(it.activationKey).isNull()
            }
            .verifyComplete()
    }


    @Test
    @WithMockUser("johndoe")
    fun `get authenticated user profile`() {
        client.get()
            .uri("/api/v1/accounts/current")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").value(IsEqualIgnoringCase("johndoe@example.com"))
            .jsonPath("$.username").value(IsEqualIgnoringCase("johndoe"))
    }
}