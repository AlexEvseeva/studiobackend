package ua.rikutou.studiobackend.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ua.rikutou.studiobackend.data.user.User

object UserTable : IntIdTable("users") {
    val name = varchar("name", 200)
    val password = varchar("password", 200)
    val salt = varchar("salt", 200)
}

class UserDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDao>(UserTable)

    var name by UserTable.name
    var password by UserTable.password
    var salt by UserTable.salt
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: UserDao) =
    User(
        id = dao.id.value,
        name = dao.name,
        password = dao.password,
        salt = dao.salt
    )