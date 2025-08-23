package com.ablil.minitrello.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.util.Base64
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@Configuration
class JwtConfig {

    @Bean
    fun jwtDecoder(
        @Value("\${spring.security.oauth2.secret-key}") secret: String
    ): ReactiveJwtDecoder = NimbusReactiveJwtDecoder
        .withSecretKey(buildSecretKey(secret))
        .macAlgorithm(MacAlgorithm.HS256)
        .build()


    @Bean
    fun jwtEncoder(
        @Value("\${spring.security.oauth2.secret-key}") secret: String
    ): JwtEncoder =
        NimbusJwtEncoder(ImmutableSecret(buildSecretKey(secret)))

    private fun buildSecretKey(secret: String) = Base64.from(secret).decode().let {
        SecretKeySpec(
            it, 0, it.size, MacAlgorithm.HS256.name
        )
    }
}