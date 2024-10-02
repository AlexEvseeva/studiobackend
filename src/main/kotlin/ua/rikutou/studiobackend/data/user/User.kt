package ua.rikutou.studiobackend.data.user

data class User(
    val id: Int? = null,
    val name: String,
    val password: String,
    val salt: String
)
