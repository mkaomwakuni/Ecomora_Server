package est.tunzo.cyberpros.server.domain.repository

import est.tunzo.cyberpros.server.domain.model.services.Services

interface ServicesRepository {
    suspend fun create(service: Services): Services
    suspend fun findById(id: String): Services?
    suspend fun findAll(): List<Services>
    suspend fun findByCategory(category: String): List<Services>
    suspend fun findActiveServices(): List<Services>
    suspend fun update(id: String, service: Services): Services?
    suspend fun delete(id: String): Boolean
    suspend fun toggleActive(id: String): Boolean
}