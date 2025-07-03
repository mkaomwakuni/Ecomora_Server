package est.ecomora.server.data.repository.cart

import est.ecomora.server.domain.model.cart.CartItem

interface CartDao {
    suspend fun getAllCart(): List<CartItem>?
    suspend fun getCartItemByUserId(id: Long): CartItem?
    suspend fun insertCartItem(
        productId: Long,
        userId: Long,
        quantity: Int
    ): CartItem?
    suspend fun updateCartItem(
        cartId: Int,
        productId: Long,
        userId: Long,
        quantity: Int
    ): Int

    suspend fun updateCart(
        cartId: Int,
        productId: Long,
        userId: Long,
        quantity: Int
    )

    suspend fun deleteCartItemByUserId(id: Long): Int

    suspend fun getCartByUserId(id: Long): List<CartItem>?

    suspend fun getCartItemByCartId(id: Int): CartItem?

    suspend fun deleteCartItemByCartId(id: Int): Int?

}