package com.unluckygbs.recipebingo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val image: String,
)
