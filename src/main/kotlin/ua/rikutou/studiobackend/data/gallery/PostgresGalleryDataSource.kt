package ua.rikutou.studiobackend.data.gallery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.rikutou.studiobackend.data.location.LocationDataSource
import java.sql.Connection

class PostgresGalleryDataSource(private val connection: Connection) : GalleryDataSource {
    companion object {
        private const val createTableGallery = "CREATE TABLE IF NOT EXISTS gallery(galleryId SERIAL PRIMARY KEY, url VARCHAR(300))"
        private const val createTableLocationToGallery = "CREATE TABLE IF NOT EXISTS location_to_gallery(locationId int not null, galleryId int not null, PRIMARY KEY(galleryId, locationId))"
        private const val getGalleryByLocationId = "SELECT * FROM gallery AS g, location_to_gallery AS lg WHERE g.galleryId = lg.galleryId AND lg.locationId = ?"
    }

    init {
        connection
            .createStatement()
            .executeUpdate(createTableGallery)

        connection
            .createStatement()
            .executeUpdate(createTableLocationToGallery)
    }

    override suspend fun getGalleryByLocationId(locationId: Int): List<Gallery> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(getGalleryByLocationId)
        statement.setInt(1, locationId)
        val result = statement.executeQuery()
        return@withContext mutableListOf<Gallery>().apply {
            while (result.next()) {
                add(
                    Gallery(
                        galleryId = result.getInt("galleryId"),
                        url = result.getString("url")
                    )
                )
            }
        }
    }

}