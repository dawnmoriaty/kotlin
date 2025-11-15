package com.financial.data.repository.impl

import com.financial.data.database.dao.UserEntity
import com.financial.data.database.tables.Users
import com.financial.data.model.User
import com.financial.data.repository.IUserRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class UserRepository : IUserRepository {

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UserEntity.findById(id)?.toModel()
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UserEntity.find { Users.email eq email }.firstOrNull()?.toModel()
    }

    override suspend fun findByUsername(username: String): User? = newSuspendedTransaction {
        UserEntity.find { Users.username eq username }.firstOrNull()?.toModel()
    }

    override suspend fun findByGoogleId(googleId: String): User? = newSuspendedTransaction {
        UserEntity.find { Users.idGoogle eq googleId }.firstOrNull()?.toModel()
    }

    override suspend fun findByFacebookId(facebookId: String): User? = newSuspendedTransaction {
        UserEntity.find { Users.idFacebook eq facebookId }.firstOrNull()?.toModel()
    }

    override suspend fun create(
        username: String,
        email: String,
        passwordHash: String?,
        idGoogle: String?,
        idFacebook: String?
    ): User = newSuspendedTransaction {
        val user = UserEntity.new {
            this.username = username
            this.email = email
            this.passwordHash = passwordHash
            this.idGoogle = idGoogle
            this.idFacebook = idFacebook
        }
        user.toModel()
    }

    override suspend fun update(user: User): User = newSuspendedTransaction {
        val entity = UserEntity.findById(user.id) ?: throw IllegalArgumentException("User not found")
        entity.apply {
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            idGoogle = user.idGoogle
            idFacebook = user.idFacebook
            isBlocked = user.isBlocked
        }
        entity.toModel()
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        val entity = UserEntity.findById(id) ?: return@newSuspendedTransaction false
        entity.delete()
        true
    }
}