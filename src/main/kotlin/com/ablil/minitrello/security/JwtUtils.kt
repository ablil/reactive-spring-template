package com.ablil.minitrello.security

import com.ablil.minitrello.users.UserDocument
import java.time.Duration
import java.time.Instant
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import reactor.core.publisher.Mono

object JwtUtils {

    fun generateToken(user: UserDocument, encode: JwtEncoder): Mono<String> =
        encode.encode(
            JwtEncoderParameters.from(
                buildJwsHeaders(),
                buildPayload(user)
            )
        )?.tokenValue?.let { Mono.just(it) }
            ?: Mono.empty()

    fun buildJwsHeaders() = JwsHeader.with(MacAlgorithm.HS256).build()

    fun buildPayload(user: UserDocument): JwtClaimsSet {
        return JwtClaimsSet
            .builder()
            .subject(user.username)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(Duration.ofHours(1)))
            .claim("roles", emptySet<String>())
            .build()
    }
}