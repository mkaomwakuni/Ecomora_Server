package est.ecomora.server.data.repository.services

import est.ecomora.server.domain.model.services.EServices

interface EservicesDao {
    suspend fun insertService(
        name: String,
        description: String,
        price: Long,
        offered: Long,
        categoryName: String,
        categoryId: Long,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String,
        userId: Long,
        discount: Long = 0,
        promotion: String = ""
    ): EServices?

    suspend fun updateService(
        id: Long,
        name: String,
        description: String,
        price: Long,
        offered: Long,
        categoryName: String,
        categoryId: Long,
        imageUrl: String,
        isVisible: Boolean,
        createdAt: String,
        updatedAt: String,
        userId: Long,
        discount: Long = 0,
        promotion: String = ""
    ): Int

    suspend fun getServiceById(id: Long, userId: Long): EServices?
    suspend fun getAllServicesByUserId(userId: Long): List<EServices>?
    suspend fun deleteServiceById(id: Long, userId: Long): Int

    suspend fun updateOfferedCounter(serviceId: Long, quantity: Long, userId: Long): Int?
}