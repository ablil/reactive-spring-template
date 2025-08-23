package com.ablil.minitrello.users

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document


@Document
data class UserDocument(
    @Id val username: String,
    @Indexed(unique = true) val email: String,
    val password: String,
    val status: AccountStatus,
    val roles: Set<UserRole>,
    @Indexed(unique = true) val activationKey: String? = null,
    @Indexed(unique = true) val passwordResetKey: String? = null
) {
    fun isActive(): Boolean = status == AccountStatus.ACTIVE
}

enum class AccountStatus {
    ACTIVE, INACTIVE
}

enum class UserRole {
    ADMIN, MANAGER
}