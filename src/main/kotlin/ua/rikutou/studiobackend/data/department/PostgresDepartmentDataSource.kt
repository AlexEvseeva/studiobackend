package ua.rikutou.studiobackend.data.department

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class PostgresDepartmentDataSource(private val connection: Connection) : DepartmentDataSource {
    companion object {
        private const val table = "department"
        private const val departmentId = "departmentId"
        private const val type = "type"
        private const val workHours = "workHours"
        private const val contactPerson = "contactPerson"
        private const val studioId = "studioId"


        private const val createTableDepart = "CREATE TABLE IF NOT EXISTS $table ($departmentId SERIAL PRIMARY KEY, $type VARCHAR(100), $workHours VARCHAR(100), $contactPerson VARCHAR(200), $studioId INTEGER)"
        private const val insertDepartment = "INSERT INTO $table ($type, $workHours, $contactPerson, $studioId) VALUES (?, ?, ?, ?)"
        private const val updateDepartment = "UPDATE $table SET $type = ?, $workHours = ?, $contactPerson = ?, $studioId = ? WHERE $departmentId = ?"
        private const val deleteDepartment = "DELETE FROM $table WHERE $departmentId = ?"
        private  const val getDepartmentById = "SELECT * FROM $table WHERE $departmentId = ?"
        private const val getDepartmentByType = "SELECT * FROM $table WHERE $type ILIKE ? LIMIT 1"
        private const val getAllDepartments = "SELECT * FROM $table WHERE $studioId = ?"
        private const val getDepartmentsFiltered = "SELECT * FROM $table WHERE $studioId = ? AND $type ILIKE ?"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableDepart)
    }

    override suspend fun insertUpdateDepartment(department: Department): Int? = withContext(Dispatchers.IO) {
//        getDepartmentByType(department.type)?.let {
//            return@withContext null
//        }
        val statement = if (department.departmentId != null) {
            connection.prepareStatement(updateDepartment).apply {
                setString(1, department.type)
                setString(2, department.workHours)
                setString(3, department.contactPerson)
                setInt(4, department.studioId)
                setInt(5, department.departmentId)
            }
        } else {
            connection.prepareStatement(insertDepartment, Statement.RETURN_GENERATED_KEYS).apply {
                setString(1, department.type)
                setString(2, department.workHours)
                setString(3, department.workHours)
                setInt(4, department.studioId)
            }
        }
        statement.executeUpdate()

        return@withContext if (department.departmentId != null) {
            department.departmentId
        } else if(statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun getDepartmentById(id: Int): Department? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getDepartmentById)
        statement.setInt(1, id)
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            Department(
                departmentId = result.getInt(departmentId),
                type = result.getString(type),
                workHours = result.getString(workHours),
                contactPerson = result.getString(contactPerson),
                studioId = result.getInt(studioId)
            )
        } else null
    }

    override suspend fun getAllDepartments(studioId: Int, search: String?): List<Department> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(search?.let {
            getDepartmentsFiltered
        } ?: getAllDepartments)
        statement.setInt(1, studioId)
        search?.let {
            statement.setString(2, "%$it%")
        }

        val result = statement.executeQuery()
        return@withContext mutableListOf<Department>().apply {
            while (result.next()) {
                add(
                    Department(
                        departmentId = result.getInt(departmentId),
                        type = result.getString(type),
                        workHours = result.getString(workHours),
                        contactPerson = result.getString(contactPerson),
                        studioId = result.getInt( studioId)
                    )
                )
            }
        }
    }

    override suspend fun getDepartmentByType(type: String): Department? = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getDepartmentByType)
        statement.setString(1, "%$type%")
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            Department(
                departmentId = result.getInt(departmentId),
                type = result.getString(type),
                workHours = result.getString(workHours),
                contactPerson = result.getString(contactPerson),
                studioId = result.getInt(studioId)
            )
        } else null
    }

    override suspend fun deleteDepartment(id: Int): Unit = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(deleteDepartment)
        statement.setInt(1, id)
        statement.execute()
    }
}