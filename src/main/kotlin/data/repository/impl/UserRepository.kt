package com.financial.data.repository.impl

import com.financial.data.database.dao.UserEntity
import com.financial.data.database.tables.Users
import com.financial.data.model.User
import com.financial.data.repository.IUserRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepository : IUserRepository {

    override suspend fun findById(id: UUID): User? = transaction {
        UserEntity.findById(id)?.toModel()
    }

    override suspend fun findByEmail(email: String): User? = transaction {
        UserEntity.find { Users.email eq email }.firstOrNull()?.toModel()
    }

    override suspend fun findByUsername(username: String): User? = transaction {
        UserEntity.find { Users.username eq username }.firstOrNull()?.toModel()
    }

    override suspend fun findByGoogleId(googleId: String): User? = transaction {
        UserEntity.find { Users.idGoogle eq googleId }.firstOrNull()?.toModel()
    }

    override suspend fun findByFacebookId(facebookId: String): User? = transaction {
        UserEntity.find { Users.idFacebook eq facebookId }.firstOrNull()?.toModel()
    }

    override suspend fun create(
        username: String,
        email: String,
        passwordHash: String?,
        idGoogle: String?,
        idFacebook: String?
    ): User = transaction {
        val user = UserEntity.new {
            this.username = username
            this.email = email
            this.passwordHash = passwordHash
            this.idGoogle = idGoogle
            this.idFacebook = idFacebook
        }
        user.toModel()
    }

    override suspend fun update(user: User): User = transaction {
        val entity = UserEntity.findById(user.id) ?: throw IllegalArgumentException("User not found")
        entity.apply {
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            idGoogle = user.idGoogle
            idFacebook = user.idFacebook
            isBlocked = user.isBlocked
            // updatedAt sẽ được trigger cập nhật tự động nếu bạn có DB trigger
        }
        entity.toModel()
    }

    override suspend fun delete(id: UUID): Boolean = transaction {
        val entity = UserEntity.findById(id) ?: return@transaction false
        entity.delete()
        true
    }
}