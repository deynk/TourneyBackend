package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class UserVerificationModel (
    val id: Long,
    val nickname: String,
    val email: String,
    val passwordHashed: String,
    val photo: Int
)