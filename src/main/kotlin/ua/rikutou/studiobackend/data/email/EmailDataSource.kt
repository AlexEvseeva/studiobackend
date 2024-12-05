package ua.rikutou.studiobackend.data.email

interface EmailDataSource {
    suspend fun insertEmail(email: Email): Int?
    suspend fun deleteEmail(emailId: Int)
}