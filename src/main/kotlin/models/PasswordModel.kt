package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordModel(
    val password: String
)