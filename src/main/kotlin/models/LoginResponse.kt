package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse (
    val token: String,
    val user: UserModel
)