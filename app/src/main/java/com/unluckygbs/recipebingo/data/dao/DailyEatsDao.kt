package com.unluckygbs.recipebingo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEatsDao {
    @Query("DELETE FROM daily_eats")
    suspend fun clearAll()
}