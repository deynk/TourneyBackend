package com.example

import com.example.entities.Tournament
import org.jetbrains.exposed.v1.jdbc.Database
import com.example.DatabaseHelper.*
import com.example.DatabaseHelper.Tournaments.code
import com.example.DatabaseHelper.Tournaments.creatorId
import com.example.DatabaseHelper.Tournaments.creatorNickname
import com.example.DatabaseHelper.Tournaments.date
import com.example.DatabaseHelper.Tournaments.game
import com.example.DatabaseHelper.Tournaments.id
import com.example.DatabaseHelper.Tournaments.location
import com.example.DatabaseHelper.Tournaments.maxParticipants
import com.example.DatabaseHelper.Tournaments.name
import com.example.DatabaseHelper.Tournaments.prize
import com.example.DatabaseHelper.Tournaments.status
import com.example.DatabaseHelper.Tournaments.thumbnail
import com.example.DatabaseHelper.Tournaments.type
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

class TournamentService(val database: Database) {
    fun getTournamentById(tournamentId: Long): Tournament? {
        return Tournaments.selectAll().where{ Tournaments.id eq tournamentId }.firstOrNull()?.toTournament();
    }
    fun getTournamentsById(tournamentIds: List<Long>): List<Tournament> {
        val tournaments = mutableListOf<Tournament>()
        tournamentIds.forEach { it -> getTournamentById(it)?.let { tournaments.add(it) } }
        return tournaments
    }


    fun ResultRow.toTournament(): Tournament {
        return Tournament(
            id = this[id],
            name = this[name],
            game = this[game],
            creatorId = this[creatorId],
            creatorNickname = this[creatorNickname],
            maxParticipants = this[maxParticipants],
            date = this[date],
            location = this[location],
            prize = this[prize],
            code = this[code],
            type = Tournament.getTournamentTypeFromString(this[type]),
            tournamentStatus = Tournament.getTournamentStatusFromString(this[status]),
            thumbnail = this[thumbnail],
        )
    }
}