package com.example

import com.example.DatabaseHelper.Matches
import com.example.DatabaseHelper.Participants
import com.example.DatabaseHelper.Tournaments
import com.example.DatabaseHelper.UserTrnRelations
import com.example.entities.Participant
import com.example.entities.Tournament
import com.example.entities.TournamentMatch
import com.example.entities.TournamentModel
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

class TournamentService(val database: Database) {
    suspend fun getTournamentById(tournamentId: Long): Tournament? = suspendTransaction(database) {
        Tournaments.selectAll().where { Tournaments.id eq tournamentId }.firstOrNull()?.toTournament()
    }

    suspend fun getTournamentByCode(code: Int): Tournament? = suspendTransaction(database) {
        Tournaments.selectAll().where { Tournaments.code eq code }.firstOrNull()?.toTournament()
    }

    suspend fun getAllTournaments(): List<Tournament> = suspendTransaction(database) {
        Tournaments.selectAll().map { it.toTournament() }
    }

    suspend fun getTournamentsById(tournamentIds: List<Long>): List<Tournament> = suspendTransaction(database) {
        if (tournamentIds.isEmpty()) return@suspendTransaction emptyList()
        Tournaments.selectAll().where { Tournaments.id inList tournamentIds }.map { it.toTournament() }
    }

    suspend fun insertTournament(tournament: TournamentModel): Long = suspendTransaction(database) {
        val newTournament = Tournaments.insert {
            if (tournament.id > 0) it[Tournaments.id] = tournament.id
            it[name] = tournament.name
            it[game] = tournament.game
            it[creatorId] = tournament.creatorId
            it[creatorNickname] = tournament.creatorNickname
            it[maxParticipants] = tournament.maxParticipants
            it[date] = tournament.date
            it[location] = tournament.location
            it[prize] = tournament.prize
            it[code] = tournament.code
            it[type] = Tournament.getTournamentTypeString(tournament.type)
            it[status] = Tournament.getTournamentStatusString(tournament.tournamentStatus)
            it[thumbnail] = tournament.thumbnail
        }

        tournament.id = newTournament[Tournaments.id]
        createCreatorRelationship(tournament)
        replaceParticipants(tournament.id, tournament.participantList)
        replaceMatches(tournament.id, tournament.matches)

        tournament.id
    }

    suspend fun updateTournament(tournament: TournamentModel): Boolean = suspendTransaction(database) {
        val updatedRows = Tournaments.update({ Tournaments.id eq tournament.id }) {
            it[name] = tournament.name
            it[game] = tournament.game
            it[creatorId] = tournament.creatorId
            it[creatorNickname] = tournament.creatorNickname
            it[maxParticipants] = tournament.maxParticipants
            it[date] = tournament.date
            it[location] = tournament.location
            it[prize] = tournament.prize
            it[code] = tournament.code
            it[type] = Tournament.getTournamentTypeString(tournament.type)
            it[status] = Tournament.getTournamentStatusString(tournament.tournamentStatus)
            it[thumbnail] = tournament.thumbnail
        }

        if (updatedRows > 0) {
            replaceParticipants(tournament.id, tournament.participantList)
            replaceMatches(tournament.id, tournament.matches)
        }

        updatedRows > 0
    }

    suspend fun updateParticipants(tournamentId: Long, participants: List<Participant>): Boolean = suspendTransaction(database) {
        if (!tournamentExists(tournamentId)) return@suspendTransaction false
        replaceParticipants(tournamentId, participants)
        true
    }

    suspend fun updateThumbnail(tournamentId: Long, thumbnailId: Int): Boolean = suspendTransaction(database) {
        Tournaments.update({ Tournaments.id eq tournamentId }) {
            it[thumbnail] = thumbnailId
        } > 0
    }

    suspend fun deleteTournament(tournamentId: Long): Boolean = suspendTransaction(database) {
        Tournaments.deleteWhere { Tournaments.id eq tournamentId } > 0
    }

    suspend fun addParticipant(tournamentId: Long, participant: Participant): Boolean = suspendTransaction(database) {
        val tournament = Tournaments.selectAll().where { Tournaments.id eq tournamentId }.firstOrNull()?.toTournament()
            ?: return@suspendTransaction false

        if (!tournament.hasSpace()) return@suspendTransaction false

        val alreadyJoined = participant.userId != null && Participants.selectAll().where {
            (Participants.tournamentId eq tournamentId) and (Participants.userId eq participant.userId)
        }.any()
        if (alreadyJoined) return@suspendTransaction true

        insertParticipant(tournamentId, participant)
        true
    }

    suspend fun removeParticipant(tournamentId: Long, userId: Long): Boolean = suspendTransaction(database) {
        Participants.deleteWhere {
            (Participants.tournamentId eq tournamentId) and (Participants.userId eq userId)
        } > 0
    }

    private fun createCreatorRelationship(tournament: TournamentModel) {
        val relationExists = UserTrnRelations.selectAll().where {
            (UserTrnRelations.userId eq tournament.creatorId) and
                    (UserTrnRelations.tournamentId eq tournament.id) and
                    (UserTrnRelations.type eq "CREATED")
        }.any()

        if (relationExists) return

        UserTrnRelations.insert {
            it[userId] = tournament.creatorId
            it[tournamentId] = tournament.id
            it[type] = "CREATED"
        }
    }

    private fun tournamentExists(tournamentId: Long): Boolean {
        return Tournaments.selectAll().where { Tournaments.id eq tournamentId }.any()
    }

    private fun replaceParticipants(tournamentId: Long, participants: List<Participant>) {
        Participants.deleteWhere { Participants.tournamentId eq tournamentId }
        participants.forEach { insertParticipant(tournamentId, it) }
    }

    private fun insertParticipant(tournamentId: Long, participant: Participant) {
        Participants.insert {
            if (participant.id > 0) it[Participants.id] = participant.id
            it[Participants.tournamentId] = tournamentId
            it[userId] = participant.userId
            it[nickname] = participant.nickname
            it[puntuation] = participant.puntuation?.toDouble()
        }
    }

    private fun getParticipants(tournamentId: Long): MutableList<Participant> {
        return Participants.selectAll().where { Participants.tournamentId eq tournamentId }
            .map { it.toParticipant() }
            .toMutableList()
    }

    private fun replaceMatches(tournamentId: Long, matches: List<TournamentMatch>) {
        Matches.deleteWhere { Matches.tournamentId eq tournamentId }
        matches.forEach { match ->
            Matches.insert {
                if (match.id > 0) it[Matches.id] = match.id
                it[Matches.tournamentId] = tournamentId
                it[round] = match.roundNumber
                it[player1Id] = match.participantOneId
                it[player2Id] = match.participantTwoId
                it[player1Name] = match.participantOneName
                it[player2Name] = match.participantTwoName
                it[score1] = match.scoreOne
                it[score2] = match.scoreTwo
                it[winnerId] = match.winnerId
            }
        }
    }

    private fun getMatches(tournamentId: Long): MutableList<TournamentMatch> {
        return Matches.selectAll().where { Matches.tournamentId eq tournamentId }
            .map { it.toTournamentMatch() }
            .toMutableList()
    }

    private fun ResultRow.toParticipant(): Participant {
        return Participant(
            id = this[Participants.id],
            userId = this[Participants.userId],
            nickname = this[Participants.nickname] ?: "",
            puntuation = this[Participants.puntuation]?.toFloat()
        )
    }

    private fun ResultRow.toTournamentMatch(): TournamentMatch {
        return TournamentMatch(
            id = this[Matches.id],
            tournamentId = this[Matches.tournamentId] ?: 0L,
            roundNumber = this[Matches.round] ?: 0,
            participantOneId = this[Matches.player1Id],
            participantTwoId = this[Matches.player2Id],
            participantOneName = this[Matches.player1Name] ?: "",
            participantTwoName = this[Matches.player2Name] ?: "",
            scoreOne = this[Matches.score1] ?: "0",
            scoreTwo = this[Matches.score2] ?: "0",
            winnerId = this[Matches.winnerId]
        )
    }

    private fun ResultRow.toTournament(): Tournament {
        val tournamentId = this[Tournaments.id]
        return Tournament(
            id = tournamentId,
            name = this[Tournaments.name],
            game = this[Tournaments.game],
            creatorId = this[Tournaments.creatorId],
            creatorNickname = this[Tournaments.creatorNickname],
            participantList = getParticipants(tournamentId),
            maxParticipants = this[Tournaments.maxParticipants],
            date = this[Tournaments.date],
            location = this[Tournaments.location],
            prize = this[Tournaments.prize],
            code = this[Tournaments.code],
            type = Tournament.getTournamentTypeFromString(this[Tournaments.type]),
            tournamentStatus = Tournament.getTournamentStatusFromString(this[Tournaments.status]),
            thumbnail = this[Tournaments.thumbnail],
            matches = getMatches(tournamentId),
        )
    }
}
