package com.example

import com.example.values.Values
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class DatabaseHelper {
    // Tables
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val nickname = varchar("nickname", 255)
        val email = varchar("email", 255)
        val passwordHash = varchar("password_hash", 255)
        val photo = integer("photo")

        override val primaryKey = PrimaryKey(id)
    }

    object Tournaments : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 255)
        val game = varchar("game", 255)
        val creatorId = reference("creator_id", Users.id, onDelete = ReferenceOption.SET_NULL)
        val creatorNickname = varchar("creator_nickname", 255)
        val maxParticipants = integer("max_participants")
        val date = long("date").nullable()
        val location = varchar("location", 255).nullable()
        val prize = varchar("prize", 255).nullable()
        val code = integer("code").nullable()
        val type = varchar("type", 255)
        val status = varchar("status", 255)
        val thumbnail = integer("thumbnail").default(0)

        override val primaryKey = PrimaryKey(id)
    }

    object Participants : Table() {
        val id = integer("id").autoIncrement()
        val tournamentId = reference("trn_id", Tournaments.id, onDelete = ReferenceOption.CASCADE).nullable()
        val userId = reference("user_id", Users.id, onDelete = ReferenceOption.SET_NULL).nullable()
        val nickname = varchar("nickname", 255).nullable()
        val puntuation = double("puntuation").nullable()

        override val primaryKey = PrimaryKey(id)
    }

    object Matches : Table() {
        val id = integer("id").autoIncrement()
        val tournamentId = reference("trn_id", Tournaments.id, onDelete = ReferenceOption.CASCADE).nullable()
        val round = integer("round").nullable()
        val player1Id = integer("p1_id").nullable()
        val player2Id = integer("p2_id").nullable()
        val player1Name = varchar("p1_name", 255).nullable()
        val player2Name = varchar("p2_name", 255).nullable()
        val score1 = varchar("score1", 255).nullable()
        val score2 = varchar("score2", 255).nullable()
        val winnerId = integer("winner_id").nullable()

        override val primaryKey = PrimaryKey(id)
    }

    object UserTrnRelations : Table() {
        val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
        val tournamentId = reference("trn_id", Tournaments.id, onDelete = ReferenceOption.CASCADE)
        val type = varchar("type", 20) // 'CREATED', 'FOLLOWING', 'JOINED'

        override val primaryKey = PrimaryKey(userId, tournamentId, type)
    }

    fun createConnection() : Database {
        return Database.connect(
            url = Values().dbUrl,
            driver = "org.mariadb.jdbc.Driver",
            user = Values().dbUserName,
            password = Values().dbUserPassword
        )
    }

    suspend fun createSchema(database: Database) {
        suspendTransaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(Tournaments)
            SchemaUtils.create(Participants)
            SchemaUtils.create(Matches)
            SchemaUtils.create(UserTrnRelations)
        }
    }
}