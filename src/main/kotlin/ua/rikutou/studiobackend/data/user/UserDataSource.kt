package ua.rikutou.studiobackend.data.user

interface UserDataSource {
    suspend fun getUserByUserName(name: String): User?
    suspend fun insertUser(user: User): Boolean
}