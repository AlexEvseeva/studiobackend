package ua.rikutou.studiobackend.data.department

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.section.PostgresSectionDataSource.Companion.sectionId
import ua.rikutou.studiobackend.data.section.Section
import ua.rikutou.studiobackend.data.studio.PostgresStudioDataSource
import ua.rikutou.studiobackend.data.transport.Transport
import java.sql.Connection
import java.sql.Statement
import ua.rikutou.studiobackend.data.section.PostgresSectionDataSource as section
import ua.rikutou.studiobackend.data.transport.PostgresTransportDataSource as transport

class PostgresDepartmentDataSource(private val connection: Connection) : DepartmentDataSource {
    companion object {
        const val table = "department"
        const val departmentId = "departmentId"
        const val type = "type"
        const val workHours = "workHours"
        const val contactPerson = "contactPerson"
        const val studioId = "studioId"


        private const val createTableDepart =
            """
                CREATE TABLE IF NOT EXISTS $table (
                    $departmentId SERIAL PRIMARY KEY,
                    $type VARCHAR(100),
                    $workHours VARCHAR(100),
                    $contactPerson VARCHAR(200),
                    $studioId INTEGER 
                        REFERENCES ${PostgresStudioDataSource.table} (${PostgresStudioDataSource.studioId}) 
                        ON DELETE CASCADE
                )
            """
        private const val insertDepartment = "INSERT INTO $table ($type, $workHours, $contactPerson, $studioId) VALUES (?, ?, ?, ?)"
        private const val updateDepartment = "UPDATE $table SET $type = ?, $workHours = ?, $contactPerson = ?, $studioId = ? WHERE $departmentId = ?"
        private const val deleteDepartment = "DELETE FROM $table WHERE $departmentId = ?"
        private const val getDepartmentById = "SELECT * FROM $table WHERE $departmentId = ?"
        private const val getDepartmentByType = "SELECT * FROM $table WHERE $type ILIKE ? LIMIT 1"
//        private const val getAllDepartments = """
//            SELECT
//            d.$departmentId, d.$type, d.$workHours, d.$contactPerson, d.$studioId,
//            s.${section.sectionId}, s.${section.title}, s.${section.address}, s.${section.internalPhoneNumber}, s.${section.departmentId} AS deptId
//            t.${transport.transportId}, t.${transport.type}, t.${transport.mark}, t.${transport.manufactureDate}, t.${transport.seats}, t.${transport.departmentId} AS tDeptId, t.${transport.color}, t.${transport.technicalState}
//            FROM ${table} d
//            LEFT JOIN ${section.table} s
//            ON d.${departmentId} = s.${section.departmentId}
//            LEFT JOIN ${transport.table} t
//            ON d.${departmentId} = t.${transport.departmentId}
//            WHERE d.studioid = ?
//        """
        private const val getAllDepartments = """
            SELECT 
            d.$departmentId, d.$type, d.$workHours, d.$contactPerson, d.$studioId, 
            t.${transport.transportId}, t.${transport.type} as transportType, t.${transport.mark}, t.${transport.manufactureDate}, t.${transport.seats}, t.${transport.departmentId} AS tDeptId, t.${transport.color}, t.${transport.technicalState}
            FROM ${table} d
            LEFT JOIN ${transport.table} t
            ON d.${departmentId} = t.${transport.departmentId}
            WHERE d.studioid = ?
        """
        private const val getAllDepartmentsFiltered = """
            SELECT 
            d.$departmentId, d.$type, d.$workHours, d.$contactPerson, d.$studioId, 
            s.${section.sectionId}, s.${section.title}, s.${section.address}, s.${section.internalPhoneNumber}, s.${section.departmentId} AS deptId
            t.${transport.transportId}, t.${transport.type} as transportType, t.${transport.mark}, t.${transport.manufactureDate}, t.${transport.seats}, t.${transport.departmentId} AS tDeptId, t.${transport.color}, t.${transport.technicalState}
            FROM ${table} d
            LEFT JOIN ${section.table} s 
            ON d.${departmentId} = s.${section.departmentId}
            LEFT JOIN ${transport.table} t
            ON d.${departmentId} = t.${transport.departmentId} 
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
                getAllDepartmentsFiltered
            } ?: getAllDepartments
        )
        statement.setInt(1, studioId)
        search?.let {
            statement.setString(2, "%$it%")
        }
        val result = statement.executeQuery()

        return@withContext mutableMapOf<Department, Pair<List<Section>, List<Transport>> >().apply {
            while (result.next()) {
                val dept = Department(
                    departmentId = result.getInt(departmentId),
                    type = result.getString(type),
                    workHours = result.getString(workHours),
                    contactPerson = result.getString(contactPerson),
                    studioId = result.getInt(PostgresDepartmentDataSource.studioId),
                )
//                val section = if(result.getInt(section.sectionId) != 0 ) {
//                    Section(
//                        sectionId = result.getInt(sectionId),
//                        title = result.getString(section.title),
//                        address = result.getString(section.address),
//                        internalPhoneNumber = result.getString(section.internalPhoneNumber),
//                        departmentId = result.getInt("deptId"),
//                    )
//                } else null
                val section: Section? = null

                val transport = if(result.getInt(transport.transportId) != 0 ) {
                    Transport (
                        transportId = result.getInt(transport.transportId),
                        type = result.getString("transportType"),
                        mark = result.getString(transport.mark),
                        manufactureDate = result.getDate(transport.manufactureDate).time,
                        seats = result.getInt(transport.seats),
                        departmentId = result.getInt("tDeptId"),
                        color = result.getString(transport.color),
                        technicalState = result.getString(transport.technicalState),
                    )
                } else null

                println("--------- ${transport}")

                val listOfSections: List<Section> = this[dept]?.first ?: emptyList()
                val listOfTransport: List<Transport> = this[dept]?.second ?: emptyList()

                when {
                    section != null && !this.containsKey(dept) -> {
                        this[dept] = Pair(listOf(section), emptyList())
                    }
                    section != null && this.containsKey(dept) && this[dept]?.first?.isNotEmpty() == true -> {
                        this[dept] = Pair(
                            mutableListOf<Section>().apply {
                                addAll(listOfSections)
                                add(section)
                            },
                            this[dept]?.second ?: emptyList()
                        )
                    }
                    else -> {
                        this[dept] = Pair(listOfSections, this[dept]?.second ?: emptyList())
                    }
                }

                when {
                    transport != null && !this.containsKey(dept) -> {
                        this[dept] = Pair(emptyList(), listOf(transport))
                    }
                    transport != null && this.containsKey(dept) && this[dept]?.second?.isNotEmpty() == true -> {
                        this[dept] = Pair(
                            this[dept]?.first ?: emptyList(),
                            mutableListOf<Transport>().apply {
                                addAll(listOfTransport)
                                add(transport)
                            }
                        )
                    }
                    else -> {
                        this[dept] = Pair (this[dept]?.first ?: emptyList(), listOfTransport)
                    }
                }

            }
        }.map { it.key.copy(sections = it.value.first, transport = it.value.second) }
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