package ua.rikutou.studiobackend.data.actor

import ua.rikutou.studiobackend.data.actorFilm.ActorFilm
import ua.rikutou.studiobackend.data.email.Email
import ua.rikutou.studiobackend.data.film.Film
import ua.rikutou.studiobackend.data.phone.Phone
import ua.rikutou.studiobackend.data.phoneConnect.PhoneActor

data class ActorRelation(
    val films: MutableSet<Film> = mutableSetOf(),
    val actorToFilms: MutableSet<ActorFilm> = mutableSetOf(),
    val phones: MutableSet<Phone> = mutableSetOf(),
    val emails: MutableSet<Email> = mutableSetOf()
)
