package est.ecomora.server.domain.repository.cart

import est.ecomora.server.data.local.table.DatabaseFactory
import est.ecomora.server.data.local.table.cart.CartTable
import est.ecomora.server.data.repository.cart.CartDao
import est.ecomora.server.domain.model.cart.CartItem
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class CartRepositoryImpl: CartDao {
    override suspend fun getAllCart(): List<CartItem>? {
        return DatabaseFactory.dbQuery {
            CartTable.selectAll().mapNotNull {
                rowToCartItem(it)
            }
        }
    }

    override suspend fun getCartItemByUserId(id: Long): CartItem? {
        return DatabaseFactory.dbQuery {
            CartTable.select(CartTable.userId.eq(id))
                .map {
                    rowToCartItem(it)
                }.singleOrNull()
        }
    }


    override suspend fun insertCartItem(
        productId: Long,
        userId: Long,
        quantity: Int
    ): CartItem? {
       return try {
           transaction {
               val insertStatement = CartTable.insert { cart->
                   cart[CartTable.productId] = productId
                   cart[CartTable.userId] = userId
                   cart[CartTable.quality] = quantity
               }
               val result = insertStatement.resultedValues?.firstOrNull()!!
               rowToCartItem(result)
               }
           } catch (e: Exception) {
               null
       }
    }

    override suspend fun updateCartItem(
        cartId: Int,
        productId: Long,
        userId: Long,
        quantity: Int
    ): Int {
        return DatabaseFactory.dbQuery {
            CartTable.update({ CartTable.cartId.eq(cartId) }) { cart ->
                cart[CartTable.quality] = quantity
                cart[CartTable.productId] = productId
                cart[CartTable.userId] = userId
                cart[CartTable.cartId] = cartId
            }
        }
    }

    override suspend fun updateCart(
        cartId: Int,
        productId: Long,
        userId: Long,
        quantity: Int
    ) {
        return DatabaseFactory.dbQuery {
            CartTable.update({ CartTable.productId.eq(productId) and CartTable.userId.eq(userId) }) { cart ->
                cart[CartTable.quality] = quantity
                cart[CartTable.productId] = productId
                cart[CartTable.userId] = userId
                cart[CartTable.cartId] = cartId
            }
        }
    }

    override suspend fun deleteCartItemByUserId(id: Long): Int {
            return DatabaseFactory.dbQuery {
                CartTable.deleteWhere { CartTable.userId.eq(id) }
            }
        }

    override suspend fun getCartByUserId(id: Long): List<CartItem>? {
        return DatabaseFactory.dbQuery {
            CartTable.select(CartTable.userId.eq(id))
                .mapNotNull { rowToCartItem(it) }
        }
    }

    override suspend fun getCartItemByCartId(id: Int): CartItem? {
        return DatabaseFactory.dbQuery {
            CartTable.select(CartTable.cartId.eq(id))
                .map { rowToCartItem(it) }
                .singleOrNull()
        }
    }

    override suspend fun deleteCartItemByCartId(id: Int): Int? {
        return DatabaseFactory.dbQuery {
            CartTable.deleteWhere { CartTable.cartId.eq(id) }
        }
    }


    private fun rowToCartItem(row: ResultRow?): CartItem? {
            if (row == null) {
                return null
            }
            return CartItem(
                productId = row[CartTable.productId],
                userId = row[CartTable.userId],
                quantity = row[CartTable.quality],
                cartId = row[CartTable.cartId]
            )
        }
    }