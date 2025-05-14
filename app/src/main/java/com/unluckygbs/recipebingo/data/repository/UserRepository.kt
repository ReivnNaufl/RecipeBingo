package com.unluckygbs.recipebingo.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.unluckygbs.recipebingo.data.dao.UserDao
import com.unluckygbs.recipebingo.data.entity.UserEntity
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {
    suspend fun getLocalUser(): UserEntity? {
        return userDao.getUser()
    }

    suspend fun saveUserLocally(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun syncUserToFirestore(user: UserEntity) {
        try {
            val docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)

            val data = mapOf(
                "username" to user.username,
                "email" to user.email,
                "profileImageBase64" to user.profileImgBase64
            )

            docRef.update(data).await()
        } catch (e: Exception) {
            Log.e("Sync", "Failed to sync user to Firestore: ${e.message}")
        }
    }
}