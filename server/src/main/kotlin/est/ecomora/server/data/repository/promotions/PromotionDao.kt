package est.ecomora.server.data.repository.promotions

import est.ecomora.server.domain.model.promotions.Promotions

interface PromotionDao {
    suspend fun insertPromo(
        userId: Long,
        title: String,
        description: String,
        imageUrl: String,
        startDate: Long,
        endDate: Long,
        enabled: Boolean
    ): Promotions?

    suspend fun updatePromo(
        id: Long,
        userId: Long,
        title: String,
        description: String,
        imageUrl: String,
        startDate: Long,
        endDate: Long,
        enabled: Boolean
    ): Promotions?

    suspend fun getAllPromotions(): List<Promotions>?
    suspend fun getPromotionById(id: Long): Promotions?
    suspend fun deletePromotionById(id: Long): Int
}