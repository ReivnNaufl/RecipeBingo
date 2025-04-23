package com.unluckygbs.recipebingo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.unluckygbs.recipebingo.data.dataclass.AnalyzedInstruction
import com.unluckygbs.recipebingo.data.dataclass.Ingredient
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import com.unluckygbs.recipebingo.data.dataclass.Nutrition
import com.unluckygbs.recipebingo.data.dataclass.RecipeIngredient

@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String,
    val isBookmarked: Boolean = false,
    val nutrition: List<Nutrient>,
    val extendedIngredient: List<RecipeIngredient>,
    val analyzedInstruction: List<AnalyzedInstruction>
)