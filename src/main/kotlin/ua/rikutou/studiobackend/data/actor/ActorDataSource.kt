package ua.rikutou.studiobackend.data.actor

interface ActorDataSource {
    suspend fun insertUpdateActors(actor: Actor): Int?
    suspend fun getActorById(id: Int): Actor?
    suspend fun getAllActors(studioId: Int, search: String?): List<Actor>
    suspend fun deleteById(actorId: Int)
}