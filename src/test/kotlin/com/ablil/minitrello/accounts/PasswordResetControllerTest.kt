package com.ablil.minitrello.accounts

import com.ablil.minitrello.PersistenceTestConfiguration
import com.ablil.minitrello.users.AccountStatus
import com.ablil.minitrello.users.UserDocument
import com.ablil.minitrello.users.UsersRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier

@SpringBootTest(classes = [PersistenceTestConfiguration::class])
@AutoConfigureWebTestClient
class PasswordResetControllerTest
@Autowired constructor(
    val client: WebTestClient,
    val usersRepository: UsersRepository,
    val objectMapper: ObjectMapper,
    val passwordEncoder: PasswordEncoder,
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
    fun `request password reset`() {
        client.post()
            .uri("/api/v1/requestpasswordreset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(EmailWrapper("johndoe@example.com")))
            .exchange()
            .expectStatus().isNoContent

        StepVerifier.create(usersRepository.findById("johndoe"))
            .assertNext { assertThat(it.passwordResetKey).isNotBlank() }
            .verifyComplete()
    }


    @Test
    fun `reset password`() {
        usersRepository.findById("johndoe")
            .map { it.copy(passwordResetKey = "randomkey") }
            .flatMap { usersRepository.save(it) }
            .block()

        client.post()
            .uri("/api/v1/resetpassword")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                objectMapper.writeValueAsString(
                    ResetPasswordRequest(
                        key = "randomkey",
                        newPassword = "mynewpassword"
                    )
                )
            )
            .exchange()
            .expectStatus().isNoContent


        StepVerifier.create(usersRepository.findById("johndoe"))
            .assertNext {
                assertThat(it.passwordResetKey).isNull()
                assertThat(passwordEncoder.matches("mynewpassword", it.password)).isTrue()
            }
            .verifyComplete()
    }

    @Test
    @WithMockUser("johndoe")
    fun `update account password`() {
        client.put()
            .uri("/api/v1/updatepassword")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                objectMapper.writeValueAsString(
                    UpdatePasswordRequest(
                        oldPassword = "supersecurepassword",
                        newPassword = "mynewpassword"
                    )
                )
            )
            .exchange()
            .expectStatus().isNoContent

        StepVerifier.create(usersRepository.findById("johndoe"))
            .assertNext {
                assertThat(passwordEncoder.matches("mynewpassword", it.password)).isTrue()
            }
            .verifyComplete()
    }

}