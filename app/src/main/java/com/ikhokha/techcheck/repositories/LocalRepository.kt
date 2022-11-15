package com.ikhokha.techcheck.repositories

import android.database.sqlite.SQLiteConstraintException
import com.ikhokha.techcheck.data.daos.ProductDao
import com.ikhokha.techcheck.data.entities.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val productDao: ProductDao
) {

    val basketItems = productDao.getBasket()

    suspend fun insertProduct(product: Product) {
        try {
            productDao.insert(product)
        } catch (e: SQLiteConstraintException) {
            productDao.updateItem(product.id)
        }
    }

    suspend fun deleteAll() = productDao.deleteAll()

}