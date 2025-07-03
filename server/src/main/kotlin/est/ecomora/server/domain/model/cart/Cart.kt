package est.ecomora.server.domain.model.cart

import est.ecomora.server.domain.model.products.Product
import kotlinx.serialization.Serializable

@Serializable
data class CartItem (
    val cartId: Int,
    val productId: Long,
    val userId: Long,
    val quantity: Int,
)