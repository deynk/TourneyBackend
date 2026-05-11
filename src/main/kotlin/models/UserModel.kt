package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class UserModel (
    val id: Long,
    val nickname: String,
    val email: String,
    val photo: Int
)