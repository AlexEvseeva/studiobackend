package ua.rikutou.studiobackend.data.user

import ua.rikutou.studiobackend.db.UserDao
import ua.rikutou.studiobackend.db.UserTable
import ua.rikutou.studiobackend.db.daoToModel
import ua.rikutou.studiobackend.db.suspendTransaction

class PostgresUserDataSource : UserDataSource {
    override suspend fun getUserByUserName(name: String): User? =
        suspendTransaction {
            UserDao
                .find {UserTable.name eq name}
                .firstOrNull()
                ?.let { daoToModel(it) }
        }

    override suspend fun insertUser(user: User): Boolean {
        val existingUser = getUserByUserName(user.name)
        if (existingUser != null){
            return false
        } else {
            suspendTransaction {
                UserDao.new {
                    name = user.name
                    password = user.password
                    salt = user.salt
                }
            }
            return true
        }
    }
}