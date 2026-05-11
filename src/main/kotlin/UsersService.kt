package com.example

import com.example.models.UserModel
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

@Serializable
data class NewUserModel(
    val nickname: String,
    val email: String,
    val passwordHash: String,
    val photo: Int
)

class ExposedUserService(val database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val nickname = varchar("nickname", 255)
        val email = varchar("email", 255)
        val passwordHash = varchar("password_hash", 255)
        val photo = integer("photo")

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun createSchema() {
        suspendTransaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun create(user: NewUserModel): Int = suspendTransaction(database) {
        val newRecord = Users.insert {
            it[nickname] = user.nickname
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[photo] = user.photo
        }
        newRecord[Users.id]
    }

    suspend fun findUser(email: String): UserModel? = suspendTransaction(database) {
        // TODO: Arreglar esto
        //val result = Users.select{ Users.email eq email }.firstOrNull()
        val resultt = Users.selectAll().where { Users.email eq email }.singleOrNull().
    }

    suspend fun read(id: Int): NewUserModel? {
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

    suspend fun update(id: Int, user: NewUserModel) {
        suspendTransaction(database) {
            Users.update({ Users.id eq id }) {
                it[nickname] = user.nickname
            }
        }
    }

    suspend fun delete(id: Int) {
        suspendTransaction(database) { Users.deleteWhere { Users.id.eq(id) } }
    }

}
