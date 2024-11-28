package ua.rikutou.studiobackend.data.department

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.section.PostgresSectionDataSource.Companion.sectionId
import ua.rikutou.studiobackend.data.section.Section
import java.sql.Connection
import java.sql.Statement
import ua.rikutou.studiobackend.data.section.PostgresSectionDataSource as section

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
        private const val getDepartmentById = "SELECT * FROM $table WHERE $departmentId = ?"
        private const val getDepartmentByType = "SELECT * FROM $table WHERE $type ILIKE ? LIMIT 1"
        private const val getAllDepartmentsWithSections = """
            SELECT 
            d.$departmentId, d.$type, d.$workHours, d.$contactPerson, d.$studioId, 
            s.${section.sectionId}, s.${section.title}, s.${section.address}, s.${section.internalPhoneNumber}, s.${section.departmentId} AS deptId
            FROM ${table} d
            LEFT JOIN ${section.table} s 
            ON d.${departmentId} = s.${section.departmentId} 
            WHERE d.studioid = ?
        """

        private const val getAllDepartmentsWithSectionsFiltered = """
            SELECT 
            d.$departmentId, d.$type, d.$workHours, d.$contactPerson, d.$studioId, 
            s.${section.sectionId}, s.${section.title}, s.${section.address}, s.${section.internalPhoneNumber}, s.${section.departmentId} AS deptId
            FROM ${table} d
            LEFT JOIN ${section.table} s 
            ON d.${departmentId} = s.${section.departmentId} 
            WHERE d.studioid = ? AND $type ILIKE ?
        """
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableDepart)
        connection
            .createStatement()
            .executeUpdate(section.createTableSection)
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
                setString(3, department.contactPerson)
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
        val statement = connection.prepareStatement(
            search?.let {
                getAllDepartmentsWithSectionsFiltered
            } ?: getAllDepartmentsWithSections
        )
        statement.setInt(1, studioId)
        search?.let {
            statement.setString(2, "%$it%")
        }
        val result = statement.executeQuery()

        return@withContext mutableMapOf<Department, List<Section>>().apply {
            while (result.next()) {
                val dept = Department(
                    departmentId = result.getInt(departmentId),
                    type = result.getString(type),
                    workHours = result.getString(workHours),
                    contactPerson = result.getString(contactPerson),
                    studioId = result.getInt(PostgresDepartmentDataSource.studioId),
                )
                val section = if(result.getInt(section.sectionId) != 0 ) {
                    Section(
                        sectionId = result.getInt(sectionId),
                        title = result.getString(section.title),
                        address = result.getString(section.address),
                        internalPhoneNumber = result.getString(section.internalPhoneNumber),
                        departmentId = result.getInt("deptId"),
                    )
                } else null

                val list: List<Section> = this[dept] ?: emptyList()

                when {
                    section != null && !this.containsKey(dept) -> {
                        this[dept] = listOf(section)
                    }
                    section != null && this.containsKey(dept) && this[dept]?.isNotEmpty() == true -> {
                        this[dept] = mutableListOf<Section>().apply {
                            addAll(list)
                            add(section)
                        }
                    }
                    else -> {
                        this[dept] = list
                    }
                }

            }
        }.map { it.key.copy(sections = it.value) }
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