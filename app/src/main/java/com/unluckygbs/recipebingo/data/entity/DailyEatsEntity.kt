package com.unluckygbs.recipebingo.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import java.util.Date
import javax.annotation.Nonnull

@Entity(tableName = "daily_eats")
data class DailyEatsEntity(
    @PrimaryKey @Nonnull var date: String,
    var totalNutrition: List<Nutrient>?
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
    val id: Int,
    val amount: Int = 1,
)

data class DailyEatsWithRecipes(
    @Embedded val dailyEats: DailyEatsEntity,
    @Relation(
        parentColumn = "date",
        entityColumn = "id",
        associateBy = Junction(
            value = DailyRecipeCrossRef::class,
            parentColumn = "date",
            entityColumn = "id"
        )
    )
    val recipes: List<RecipeEntity>
)