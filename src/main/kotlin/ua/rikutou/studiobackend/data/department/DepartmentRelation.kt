package ua.rikutou.studiobackend.data.department

import ua.rikutou.studiobackend.data.email.Email
import ua.rikutou.studiobackend.data.phone.Phone
import ua.rikutou.studiobackend.data.section.Section
import ua.rikutou.studiobackend.data.transport.Transport

data class DepartmentRelation(
    val sections: MutableSet<Section> = mutableSetOf(),
    val transport: MutableSet<Transport> = mutableSetOf(),
    val phones: MutableSet<Phone> = mutableSetOf(),
    val emails: MutableSet<Email> = mutableSetOf()
)
