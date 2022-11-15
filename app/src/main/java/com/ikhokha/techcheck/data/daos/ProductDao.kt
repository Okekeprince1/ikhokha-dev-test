package com.ikhokha.techcheck.data.daos

import androidx.room.*
import com.ikhokha.techcheck.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(movie: Product): Long

    @Delete
    suspend fun delete(movie: Product)

    @Query("UPDATE product_table SET quantity = quantity + 1 WHERE id = :id")
    fun updateItem(id: String)

    @Query("SELECT * FROM product_table")
    fun getBasket(): Flow<List<Product>>

    @Query ("DELETE FROM product_table")
    fun deleteAll()
}