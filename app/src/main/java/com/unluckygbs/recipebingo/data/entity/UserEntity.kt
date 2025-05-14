package com.unluckygbs.recipebingo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val uid: String,
    val username: String,
    val email: String,
    val profileImgBase64: String
)