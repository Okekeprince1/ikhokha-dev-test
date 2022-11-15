package com.ikhokha.techcheck.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "product_table")
data class Product (
    @PrimaryKey
    var id         : String = "",
    val description : String? = null,
    val image       : String? = null,
    val price       : Double? = null,
    var quantity    : Int = 1

): Parcelable
