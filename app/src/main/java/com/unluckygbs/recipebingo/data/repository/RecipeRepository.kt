package com.unluckygbs.recipebingo.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.dao.DailyEatsDao
import com.unluckygbs.recipebingo.data.dao.RecipeDao
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import com.unluckygbs.recipebingo.data.toDailyEatsFS
import com.unluckygbs.recipebingo.data.toRecipeFS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class RecipeRepository(
    private val dao: RecipeDao,
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    suspend fun getAllRecipe(): Flow<List<RecipeEntity>> = dao.getAll()

    fun getAllBookmarkedRecipes(): Flow<List<RecipeEntity>> = dao.getAllBookmarked()

    suspend fun insertSingleRecipe(recipeEntity: RecipeEntity) {
        dao.insertRecipe(recipeEntity)
    }

    suspend fun updateOrInsertRecipe(recipeEntity: RecipeEntity, changeBookmark: Boolean) {
        if (dao.isRecipeExist(recipeEntity.id)) {
            if (changeBookmark){
                dao.updateBookmark(recipeEntity.id, recipeEntity.isBookmarked)
            }
        } else {
            dao.insertRecipe(recipeEntity)
        }
    }

    suspend fun deleteAll(){
        dao.clearAll()
    }

    suspend fun isRecipeExist(id: Int): Boolean{
        return dao.isRecipeExist(id)
    }

    suspend fun getRecipeById(id: Int): RecipeEntity {
        return dao.getRecipeById(id)
    }

    suspend fun observeBookmarkStatus(recipeId: Int): Flow<Boolean?>{
        return dao.getBookmarkStatus(recipeId)
    }

    suspend fun syncSingleRecipe(recipeEntity: RecipeEntity) {
        try {
            val ref = firestore.collection("recipes")
                .document(recipeEntity.id.toString())

            ref.set(recipeEntity.toRecipeFS()).await()
            Log.d("SyncRecipe", "Recipe ${recipeEntity.title} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("SyncRecipe", "Failed to sync daily eats: ${e.message}")
            Log.d("SyncRecipeDebug", "userId: $userId, recipeId: ${recipeEntity.id}")
        }
    }

    suspend fun isRecipeExistsInFireStore(recipeEntity: RecipeEntity): Boolean {
        try {
            val doc = firestore.collection("recipes")
                .document(recipeEntity.id.toString())
                .get()
                .await()

            return doc.exists()
        } catch (e: Exception) {
            Log.e("isRecipeExistsInFireStore", "Failed to check existing recipe: ${e.message}")
            Log.d("isRecipeExistsInFireStoreDebug", "userId: $userId, recipeId: ${recipeEntity.id}")
            return false
        }
    }

    suspend fun syncRecipeBookmarkToFireStore() {
        try {
            val bookmarks = dao.getBookmarksID()
            val ref = firestore.collection("users").document(userId)

            firestore.runTransaction { transaction ->
                transaction.update(ref, "bookmarkedRecipeIDs", bookmarks)
            }.await()

            Log.d("SyncBookmark", "Bookmark user $userId synced to FireStore.")
        } catch (e: Exception) {
            Log.e("SyncBookmark", "Failed to check existing recipe: ${e.message}")
            Log.d("SyncBookmarkDebug", "userId: $userId")
        }
    }
}