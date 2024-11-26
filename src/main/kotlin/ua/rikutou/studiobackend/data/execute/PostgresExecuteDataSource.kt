package ua.rikutou.studiobackend.data.execute

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

class PostgresExecuteDataSource(
    private val connection: Connection
) : ExecuteDataSource {
    override suspend fun execute(query: String): List<List<String>> = withContext(Dispatchers.IO) {
        val result = connection.prepareStatement(query).executeQuery()
        val numberOfColumns = result.metaData.columnCount
        val queryResult = mutableListOf<List<String>>()

        val columnNames = mutableListOf<String>().apply {
            for(index in 1 until numberOfColumns + 1) {
                add(result.metaData.getColumnName(index))
            }
        }
        queryResult.add(columnNames)

        while (result.next()) {
            val row = mutableListOf<String>().apply {
                for(index in 1 until numberOfColumns + 1) {
                    add(result.getString(index))
                }
            }
            queryResult.add(row)
        }

        return@withContext queryResult
    }
}