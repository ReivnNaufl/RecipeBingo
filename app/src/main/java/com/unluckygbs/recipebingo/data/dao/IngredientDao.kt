package com.unluckygbs.recipebingo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): LiveData<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients")
    fun getAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients")
    fun getAllAsList(): List<IngredientEntity>

    @Query("SELECT * FROM ingredients")
    suspend fun getAllOnce(): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients")
    suspend fun clearAll()
}
