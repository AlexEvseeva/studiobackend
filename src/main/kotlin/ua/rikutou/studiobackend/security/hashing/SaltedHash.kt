package ua.rikutou.studiobackend.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)
