package com.example.entities

import kotlinx.serialization.Serializable

@Serializable
data class MatchData (
    var competitorOne: CompetitorData,
    var competitorTwo: CompetitorData
)