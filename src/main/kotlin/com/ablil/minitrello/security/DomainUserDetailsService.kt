package com.ablil.minitrello.security

import com.ablil.minitrello.users.AccountStatus
import com.ablil.minitrello.users.UserRole
import com.ablil.minitrello.users.UsersRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DomainUserDetailsService(val usersRepository: UsersRepository) : ReactiveUserDetailsService {

    override fun findByUsername(username: String?): Mono<UserDetails?>? {
        return usersRepository
            .findById(requireNotNull(username))
            .map {
                User.builder()
                    .username(it.username)
                    .password(it.password)
                    .accountLocked(it.status != AccountStatus.ACTIVE)
                    .accountExpired(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .authorities(it.roles.map(UserRole::name).map(::SimpleGrantedAuthority))
                    .build()
            }
    }
}