package ua.rikutou.studiobackend.data.phone

interface PhoneDataSource {
    suspend fun insertPhone(phone: Phone): Int?
    suspend fun deletePhoneById(phoneId: Int)
}