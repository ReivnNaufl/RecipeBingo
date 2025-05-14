package com.unluckygbs.recipebingo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unluckygbs.recipebingo.data.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = 1 LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}