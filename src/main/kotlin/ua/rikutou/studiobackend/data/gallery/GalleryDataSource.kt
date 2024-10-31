package ua.rikutou.studiobackend.data.gallery

interface GalleryDataSource {
    suspend fun getGalleryByLocationId(locationId: Int): List<Gallery>

}