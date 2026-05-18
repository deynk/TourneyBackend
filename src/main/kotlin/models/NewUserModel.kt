package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class NewUserModel(
    val nickname: String,
    val email: String,
    val password: String,
    val photo: Int
)