package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class RememberPasswordModel (
    val email: String,
    val nickname: String
)
