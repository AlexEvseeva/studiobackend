package ua.rikutou.studiobackend.data.section

interface SectionDataSource {
    suspend fun insertUpdateSection(section: Section): Int?
    suspend fun getSectionById(sectionId: Int): Section?
    suspend fun getAllSections(departmentId: Int, search: String?): List<Section>
    suspend fun deleteSection(sectionId: Int)
}