package com.unluckygbs.recipebingo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unluckygbs.recipebingo.data.entity.DailyRecipeCrossRef
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipe WHERE isBookmarked = 1")
    fun getAllBookmarked(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipeEntity: RecipeEntity)

    @Query("SELECT isBookmarked FROM recipe WHERE id = :recipeId")
    fun getBookmarkStatus(recipeId: Int): Flow<Boolean?>

    @Query("SELECT EXISTS(SELECT 1 FROM recipe WHERE id = :id LIMIT 1)")
    suspend fun isRecipeExist(id: Int): Boolean

    @Query("UPDATE recipe SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Int, isBookmarked: Boolean)

    @Query("SELECT * FROM recipe WHERE id = :id")
    suspend fun getRecipeById(id: Int): RecipeEntity

    @Query("DELETE FROM recipe")
    suspend fun clearAll()
}