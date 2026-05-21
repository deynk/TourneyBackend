package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class EmailAndNickname (
    val email: String,
    val nickname: String
)
