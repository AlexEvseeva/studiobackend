package ua.rikutou.studiobackend.data.equipment

interface EquipmentDataSource {
    suspend fun insertUpdateEquipment(equipment: Equipment): Int?
    suspend fun getEquipmentByName(name: String): Equipment?
    suspend fun getEquipmentById(equipmentId: Int): Equipment?
    suspend fun getAllEquipment(studioId: Int, search: String?): List<Equipment>
    suspend fun deleteById(equipmentId: Int)
}