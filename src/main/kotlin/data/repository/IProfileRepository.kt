package com.financial.data.repository

import com.financial.data.model.Profile
import java.util.*

interface IProfileRepository {
    suspend fun findById(id: UUID): Profile?
    suspend fun findByUserId(userId: UUID): Profile?
    suspend fun create(userId: UUID): Profile
    suspend fun update(profile: Profile): Profile
    suspend fun delete(id: UUID): Boolean
}

