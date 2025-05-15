package com.unluckygbs.recipebingo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import com.unluckygbs.recipebingo.data.entity.DailyEatsEntity
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.DailyRecipeCrossRef
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyEats(dailyEats: DailyEatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: DailyRecipeCrossRef)

    @Query("SELECT date FROM daily_eats ORDER BY date DESC LIMIT 1")
    suspend fun getLatestDate(): DailyEatsWithRecipes

    @Query("SELECT EXISTS(SELECT 1 FROM daily_recipe_cross_ref WHERE date = :date AND id = :recipeId)")
    suspend fun isRecipeAlreadyAdded(date: String, recipeId: Int): Boolean

    @Transaction
    @Query("SELECT * FROM daily_eats")
    fun getAllDailyEatsWithRecipes(): Flow<List<DailyEatsWithRecipes>>

    @Transaction
    @Query("SELECT * FROM daily_eats WHERE date = :date")
    suspend fun getDailyEatsWithRecipes(date: String): DailyEatsWithRecipes?

    @Query("SELECT * FROM daily_recipe_cross_ref WHERE date = :date")
    suspend fun getDateCrossRef(date: String): List<DailyRecipeCrossRef>

    @Query("DELETE FROM daily_eats")
    suspend fun clearAll()

    @Update
    suspend fun updateDailyEats(dailyEats: DailyEatsEntity)

    @Update
    suspend fun updateCrossRef(crossRef: DailyRecipeCrossRef)

    @Query("Update daily_recipe_cross_ref SET amount = amount + 1 WHERE date = :date AND id = :id")
    suspend fun recipeAmountPlusOne(date: String, id: Int): Int

    @Query("UPDATE daily_eats SET totalNutrition = :nutrition WHERE date = :date")
    suspend fun updateDailyEatsNutrition(date: String, nutrition: List<Nutrient>)

    @Delete
    suspend fun deleteDailyEats(dailyEats: DailyEatsEntity)
}