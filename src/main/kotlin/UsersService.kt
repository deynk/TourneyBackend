package com.example

import com.example.DatabaseHelper.*
import com.example.models.NewUserModel
import com.example.models.UserModel
import com.example.models.UserVerificationModel
import com.example.utils.Security
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update


class UserService(val database: Database) {
    suspend fun create(user: NewUserModel): Long = suspendTransaction(database) {
        if (Users.select(Users.email eq user.email).count() > 0) {
            return@suspendTransaction -1L
        }

        val newRecord = Users.insert {
            it[nickname] = user.nickname
            it[email] = user.email
            it[passwordHash] = Security.encryptPassword(user.password)
            it[photo] = user.photo
        }
        newRecord[Users.id]
    }

    suspend fun findUserForLogin(email: String): UserVerificationModel? = suspendTransaction(database) {
        Users.selectAll().where { Users.email eq email }.singleOrNull()?.toUserVerificationModel()
    }
    suspend fun findUserById(userId: Long): UserModel? = suspendTransaction(database) {
        Users.selectAll().where { Users.id eq userId}.singleOrNull()?.toUserModel()
    }
    suspend fun getUserNicknameByEmail(email: String): String? = suspendTransaction(database) {
        Users.select(Users.nickname).where{Users.email eq email}.singleOrNull()?.getNickname()
    }

    suspend fun getRelationsForUser(userId: Long, type: String): List<Long> = suspendTransaction(database) {
        UserTrnRelations.selectAll().where {
                (UserTrnRelations.userId eq userId) and (UserTrnRelations.type eq type)
            }
            .map { it[UserTrnRelations.tournamentId].toLong() }
    }

    suspend fun checkPassword(userId: Long, password: String): Boolean = suspendTransaction(database){
        val passwordHash = Users.select(Users.passwordHash).where{ Users.id eq userId }.map { it[Users.passwordHash] }.singleOrNull()
        passwordHash?.let {
            Security.checkPasswords(password, it)
        } ?: false
    }
    suspend fun updatePassword(email: String, password: String): Boolean = suspendTransaction(database){
        val updatedRows = Users.update({ Users.email eq email }) { it[Users.passwordHash] = Security.encryptPassword(password) }
        updatedRows > 0 // true si se actualizó al menos un registro
    }
    suspend fun updatePassword(userId: Long, password: String): Boolean = suspendTransaction(database){

        val updatedRows = Users.update({ Users.id eq userId }) { it[Users.passwordHash] = Security.encryptPassword(password) }
        updatedRows > 0 // true si se actualizó al menos un registro
    }

    suspend fun updateAvatar(userId: Long, avatarId: Int): Boolean = suspendTransaction(database){
        val updatedRows = Users.update({ Users.id eq userId }) { it[Users.photo] = avatarId }
        updatedRows > 0 // true si se actualizó al menos un registro
    }

    fun ResultRow.toUserVerificationModel(): UserVerificationModel {
        return UserVerificationModel(
            id = this[Users.id].toLong(),
            nickname = this[Users.nickname],
            email = this[Users.email],
            passwordHashed = this[Users.passwordHash],
            photo = this[Users.photo]
        )
    }
    fun ResultRow.toUserModel(): UserModel {
        return UserModel(
            id = this[Users.id].toLong(),
            nickname = this[Users.nickname],
            email = this[Users.email],
            photo = this[Users.photo]
        )
    }

    fun ResultRow.getNickname(): String = this[Users.nickname]

    suspend fun read(id: Long): NewUserModel? {
        return suspendTransaction(database) {
            Users.selectAll()
                .where { Users.id eq id }
                .map { NewUserModel(
                    //it[Users.id].toLong(),
                    it[Users.nickname],
                    it[Users.email],
                    it[Users.passwordHash],
                    it[Users.photo]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Long, user: NewUserModel) {
        suspendTransaction(database) {
            Users.update({ Users.id eq id }) {
                it[nickname] = user.nickname
            }
        }
    }

    suspend fun delete(id: Long) {
        suspendTransaction(database) { Users.deleteWhere { Users.id.eq(id) } }
    }

}
