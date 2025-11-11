package com.financial.data.database.dao

import com.financial.data.database.tables.Profiles
import com.financial.data.model.Profile
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ProfileEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfileEntity>(Profiles)

    var userId by Profiles.userId
    var fullName by Profiles.fullName
    var avatarUrl by Profiles.avatarUrl
    var phone by Profiles.phone
    var dateOfBirth by Profiles.dateOfBirth
    var address by Profiles.address
    var bio by Profiles.bio
    var createdAt by Profiles.createdAt
    var updatedAt by Profiles.updatedAt

    fun toModel() = Profile(
        userId = id.value,
        fullName = fullName,
        avatarUrl = avatarUrl,
        phone = phone,
        dateOfBirth = dateOfBirth,
        address = address,
        bio = bio,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
