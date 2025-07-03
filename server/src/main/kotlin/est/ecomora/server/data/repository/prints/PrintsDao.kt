package est.ecomora.server.data.repository.prints

import est.ecomora.server.domain.model.electronics.Prints

interface PrintsDao {
    suspend fun insertPrint (
        name: String,
        description: String,
        price: Double,
        imageUrl: String,
        copies: Int
    ): Prints?

    suspend fun getAllPrints(): List<Prints>?

    suspend fun getPrintById(id: Long): Prints?

    suspend fun updatePrint(
        id: Long,
        name: String,
        description: String,
        price: Double,
        imageUrl: String,
        copies: Int
    ): Int?

}