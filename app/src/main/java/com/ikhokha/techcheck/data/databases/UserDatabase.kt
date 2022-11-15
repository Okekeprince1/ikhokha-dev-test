package com.ikhokha.techcheck.data.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ikhokha.techcheck.data.daos.ProductDao
import com.ikhokha.techcheck.data.entities.Product

@Database(entities = [Product::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        const val dbName = "genre_analytics"
    }
}