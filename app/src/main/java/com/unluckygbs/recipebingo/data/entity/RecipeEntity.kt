package com.unluckygbs.recipebingo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String
)