package ua.rikutou.studiobackend.data.section

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class PostgresSectionDataSource(private val connection: Connection) : SectionDataSource {
    companion object {
        const val table = "section"
        const val sectionId = "sectionId"
        const val title = "title"
        const val address = "address"
        const val internalPhoneNumber = "internalPhoneNumber"
        const val departmentId = "departmentId"

        const val createTableSection = "CREATE TABLE IF NOT EXISTS $table ($sectionId SERIAL PRIMARY KEY, $title VARCHAR(100), $address VARCHAR(100), $internalPhoneNumber VARCHAR(20), $departmentId INTEGER)"
        private const val insertSection = "INSERT INTO $table ($title, $address, $internalPhoneNumber, $departmentId) VALUES (?, ?, ?, ?)"
        private const val updateSection = "UPDATE $table SET $title = ?, $address = ?, $internalPhoneNumber = ?, $departmentId = ? WHERE $sectionId = ?"
        private const val deleteSection = "DELETE FROM $table WHERE $sectionId = ?"
        private const val getSectionById = "SELECT * FROM $table WHERE $sectionId = ?"
        private const val getAllSections = "SELECT * FROM $table WHERE $departmentId = ?"
        private const val getlAllSectionsFiltered = "SELECT * FROM $table WHERE $departmentId = ? AND ($title ILIKE ? OR $address ILIKE ?)"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableSection)
    }

    override suspend fun insertUpdateSection(section: Section): Int? = withContext(Dispatchers.IO){
        val statement = if (section.sectionId != null) {
            connection.prepareStatement(updateSection).apply {
                setString(1, section.title)
                setString(2, section.address)
                setString(3, section.internalPhoneNumber)
                setInt(4, section.departmentId)
                setInt(5, section.sectionId)
            }
        } else {
            connection.prepareStatement(insertSection, Statement.RETURN_GENERATED_KEYS).apply {
                setString(1, section.title)
                setString(2, section.address)
                setString(3, section.internalPhoneNumber)
                setInt(4, section.departmentId)
            }
        }
        statement.executeUpdate()

        return@withContext if (section.sectionId != null) {
            section.sectionId
        } else if (statement.generatedKeys.next()) {
            statement.generatedKeys.getInt(1)
        } else null
    }

    override suspend fun getSectionById(sectionId: Int): Section? = withContext(Dispatchers.IO){
        val statement = connection.prepareStatement(getSectionById)
        statement.setInt(1, sectionId)
        val result = statement.executeQuery()

        return@withContext if (result.next()) {
            Section (
                sectionId = result.getInt(sectionId),
                title = result.getString(title),
                address = result.getString(address),
                internalPhoneNumber = result.getString(internalPhoneNumber),
                departmentId = result.getInt(departmentId)
            )
        } else null
    }

    override suspend fun getAllSections(departmentId: Int, search: String?): List<Section> = withContext(Dispatchers.IO){
        val statement = connection.prepareStatement(search?.let {
            getlAllSectionsFiltered
        } ?: getAllSections)

        statement.setInt(1, departmentId)
        search?.let {
            val searchString = "%$search%"
            statement.setString(2, searchString)
            statement.setString(3, searchString)
        }

        val result = statement.executeQuery()
        return@withContext mutableListOf<Section>().apply {
            while (result.next()) {
                    add (
                        Section (
                        sectionId = result.getInt(sectionId),
                        title = result.getString(title),
                        address = result.getString(address),
                        internalPhoneNumber = result.getString(internalPhoneNumber),
                        departmentId = result.getInt(departmentId)
                    )
                )
            }
        }
    }

    override suspend fun deleteSection(sectionId: Int): Unit = withContext(Dispatchers.IO){
        val statement = connection.prepareStatement(deleteSection)
        statement.setInt(1, sectionId)
        statement.execute()
    }
}