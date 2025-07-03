package est.ecomora.server.data.repository.services

import est.ecomora.server.domain.model.services.EServices

interface EservicesDao {
    suspend fun insertService(
        name: String,
        description: String,
        price: Long,
        offered: Long,
        category: String,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String
    ): EServices?

    suspend fun updateService(
        id: Long,
        name: String,
        description: String,
        price: Long,
        offered: Long,
        category: String,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String
    ): Int

    suspend fun getServiceById(id: Long): EServices?
    suspend fun getAllServices(): List<EServices>?
    suspend fun deleteServiceById(id: Long): Int
}