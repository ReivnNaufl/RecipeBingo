package com.unluckygbs.recipebingo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import com.unluckygbs.recipebingo.data.dataclass.Nutrition
import java.util.Date

@Entity(tableName = "daily_eats")
data class DailyEatsEntity(
    @PrimaryKey val date: String,
    val totalNutrition: List<Nutrient>
)

@Entity(
    tableName = "daily_recipe_cross_ref",
    primaryKeys = ["date", "id"],
    foreignKeys = [
        ForeignKey(
            entity = DailyEatsEntity::class,
            parentColumns = ["date"],
            childColumns = ["date"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyRecipeCrossRef(
    val date: String,
    val id: String
)