package ua.rikutou.studiobackend.data.user

import ua.rikutou.studiobackend.data.user.responses.StudioUser

interface UserDataSource {
    suspend fun getUserById(userId: Int): User?
    suspend fun getUserByUserName(name: String): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun updateUser(userId: Int, studioId: Int)
    suspend fun getStudioUsersAndCandidates(studioId: Int): List<StudioUser>
}