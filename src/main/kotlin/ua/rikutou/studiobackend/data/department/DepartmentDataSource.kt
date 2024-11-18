package ua.rikutou.studiobackend.data.department

interface DepartmentDataSource {
    suspend fun insertUpdateDepartment(department: Department): Int?
    suspend fun getDepartmentById(id: Int): Department?
    suspend fun getAllDepartments(studioId: Int, search: String?): List<Department>
    suspend fun getDepartmentByType(type: String): Department?
    suspend fun deleteDepartment(id: Int)
}