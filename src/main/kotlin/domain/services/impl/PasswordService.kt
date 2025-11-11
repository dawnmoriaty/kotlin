package com.financial.domain.services.impl

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordService {
    fun hashPassword(plainText: String): String {
        return BCrypt.withDefaults().hashToString(12, plainText.toCharArray())
    }

    fun verifyPassword(plainText: String, hashed: String): Boolean {
        return BCrypt.verifyer().verify(plainText.toCharArray(), hashed).verified
    }
}