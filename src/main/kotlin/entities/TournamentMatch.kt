package com.example.entities

import kotlinx.serialization.Serializable

@Serializable
data class TournamentMatch(
    var id: Long = 0,
    var tournamentId: Long,
    var roundNumber: Int,
    var participantOneId: Long? = null, // Referencia al ID de Participante
    var participantTwoId: Long? = null,
    var participantOneName: String, // Referencia al nombre de Participante
    var participantTwoName: String,
    var scoreOne: String = "0",
    var scoreTwo: String = "0",
    var winnerId: Long? = null
)