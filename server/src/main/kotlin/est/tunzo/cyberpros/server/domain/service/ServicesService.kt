package est.tunzo.cyberpros.server.domain.service

import est.tunzo.cyberpros.server.domain.model.services.Services

interface ServicesService {
    suspend fun createService(service: Services): Result<Services>
    suspend fun getServiceById(id: String): Result<Services>
    suspend fun getAllServices(): Result<List<Services>>
    suspend fun getServicesByCategory(category: String): Result<List<Services>>
    suspend fun getActiveServices(): Result<List<Services>>
    suspend fun updateService(id: String, service: Services): Result<Services>
    suspend fun deleteService(id: String): Result<Boolean>
    suspend fun activateService(id: String): Result<Boolean>
    suspend fun deactivateService(id: String): Result<Boolean>
    suspend fun searchServices(query: String): Result<List<Services>>
}