package com.financial.data.repository.impl

import com.financial.data.database.tables.Profiles
import com.financial.data.model.Profile
import com.financial.data.repository.IProfileRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.*

class ProfileRepository : IProfileRepository {

    override suspend fun findById(id: UUID): Profile? = newSuspendedTransaction {
        Profiles.selectAll().where { Profiles.userId eq id }
            .map { rowToProfile(it) }
            .singleOrNull()
    }

    override suspend fun findByUserId(userId: UUID): Profile? = findById(userId)

    override suspend fun create(userId: UUID): Profile = newSuspendedTransaction {
        val now = LocalDateTime.now()

        Profiles.insert {
            it[Profiles.userId] = userId
            it[fullName] = null
            it[avatarUrl] = null
            it[phone] = null
            it[dateOfBirth] = null
            it[address] = null
            it[bio] = null
            it[createdAt] = now
            it[updatedAt] = now
        }

        // Return the created profile directly instead of querying again
        Profile(
            userId = userId,
            fullName = null,
            avatarUrl = null,
            phone = null,
            dateOfBirth = null,
            address = null,
            bio = null,
            createdAt = now,
            updatedAt = now
        )
    }

    override suspend fun update(profile: Profile): Profile = newSuspendedTransaction {
        val now = LocalDateTime.now()

        Profiles.update({ Profiles.userId eq profile.userId }) {
            it[fullName] = profile.fullName
            it[avatarUrl] = profile.avatarUrl
            it[phone] = profile.phone
            it[dateOfBirth] = profile.dateOfBirth
            it[address] = profile.address
            it[bio] = profile.bio
            it[updatedAt] = now
        }

        // Return updated profile with new timestamp
        profile.copy(updatedAt = now)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        Profiles.deleteWhere { Profiles.userId eq id } > 0
    }

    private fun rowToProfile(row: ResultRow): Profile {
        return Profile(
            userId = row[Profiles.userId],
            fullName = row[Profiles.fullName],
            avatarUrl = row[Profiles.avatarUrl],
            phone = row[Profiles.phone],
            dateOfBirth = row[Profiles.dateOfBirth],
            address = row[Profiles.address],
            bio = row[Profiles.bio],
            createdAt = row[Profiles.createdAt],
            updatedAt = row[Profiles.updatedAt]
        )
    }
}

