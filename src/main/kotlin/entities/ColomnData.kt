package com.example.entities

import kotlinx.serialization.Serializable


@Serializable
data class ColomnData(
    var matches: List<MatchData>
)