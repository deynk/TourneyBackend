package com.example.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.values.Values
import java.util.Date

class Auth {
    fun generateToken(userId: Long): String {
        return JWT.create()
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + Values().expirationTimeToken.toLong()))
            .sign(Algorithm.HMAC256(Values().secretKey))
    }
}