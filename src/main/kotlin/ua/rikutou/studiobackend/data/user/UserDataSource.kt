package ua.rikutou.studiobackend.data.user

interface UserDataSource {
    suspend fun getUserById(userId: Int): User?
    suspend fun getUserByUserName(name: String): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun updateUser(userId: Int, studioId: Int)
}