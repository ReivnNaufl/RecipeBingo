package com.unluckygbs.recipebingo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.unluckygbs.recipebingo.data.entity.IngredientEntity

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): LiveData<List<IngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients")
    suspend fun clearAll()
}
