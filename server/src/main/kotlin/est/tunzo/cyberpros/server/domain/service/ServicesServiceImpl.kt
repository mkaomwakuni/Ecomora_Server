package est.tunzo.cyberpros.server.domain.service

import est.tunzo.cyberpros.server.domain.model.services.Services
import est.tunzo.cyberpros.server.domain.repository.ServicesRepository

class ServicesServiceImpl(
    private val servicesRepository: ServicesRepository
) : ServicesService {

    override suspend fun createService(service: Services): Result<Services> {
        return try {
            val createdService = servicesRepository.create(service)
            Result.success(createdService)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceById(id: String): Result<Services> {
        return try {
            val service = servicesRepository.findById(id)
            if (service != null) {
                Result.success(service)
            } else {
                Result.failure(NoSuchElementException("Service with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllServices(): Result<List<Services>> {
        return try {
            val services = servicesRepository.findAll()
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServicesByCategory(category: String): Result<List<Services>> {
        return try {
            val services = servicesRepository.findByCategory(category)
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveServices(): Result<List<Services>> {
        return try {
            val services = servicesRepository.findActiveServices()
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateService(id: String, service: Services): Result<Services> {
        return try {
            val updatedService = servicesRepository.update(id, service)
            if (updatedService != null) {
                Result.success(updatedService)
            } else {
                Result.failure(NoSuchElementException("Service with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteService(id: String): Result<Boolean> {
        return try {
            val deleted = servicesRepository.delete(id)
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateService(id: String): Result<Boolean> {
        return try {
            val activated = servicesRepository.toggleActive(id)
            Result.success(activated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateService(id: String): Result<Boolean> {
        return try {
            val deactivated = servicesRepository.toggleActive(id)
            Result.success(deactivated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchServices(query: String): Result<List<Services>> {
        return try {
            val allServices = servicesRepository.findAll()
            val filteredServices = allServices.filter { service ->
                service.name.contains(query, ignoreCase = true) ||
                        service.description.contains(query, ignoreCase = true) ||
                        service.category.contains(query, ignoreCase = true)
            }
            Result.success(filteredServices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}