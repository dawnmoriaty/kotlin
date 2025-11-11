package com.financial.data.database.dao

import com.financial.data.database.tables.Users
import com.financial.data.model.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(Users)

    var username by Users.username
    var email by Users.email
    var passwordHash by Users.passwordHash
    var idFacebook by Users.idFacebook
    var idGoogle by Users.idGoogle
    var role by Users.role
    var isBlocked by Users.isBlocked
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt

    // Convert sang model
    fun toModel() = User(
        id = id.value,
        username = username,
        email = email,
        passwordHash = passwordHash,
        idFacebook = idFacebook,
        idGoogle = idGoogle,
        role = role,
        isBlocked = isBlocked,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}