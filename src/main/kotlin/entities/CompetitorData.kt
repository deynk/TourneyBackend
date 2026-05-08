package com.example.entities

import kotlinx.serialization.Serializable

@Serializable
data class CompetitorData (
    var name: String,
    var score: String
)