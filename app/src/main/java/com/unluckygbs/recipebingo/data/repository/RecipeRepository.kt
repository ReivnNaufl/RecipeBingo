package com.unluckygbs.recipebingo.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.firestore.toObjects
import com.unluckygbs.recipebingo.data.dao.DailyEatsDao
import com.unluckygbs.recipebingo.data.dao.RecipeDao
import com.unluckygbs.recipebingo.data.dataclass.Recipe
import com.unluckygbs.recipebingo.data.dataclass.RecipeFS
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import com.unluckygbs.recipebingo.data.toDailyEatsFS
import com.unluckygbs.recipebingo.data.toRecipeEntity
import com.unluckygbs.recipebingo.data.toRecipeFS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

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

    fun saveShuffledDailyRecipes(context: Context, recipes: List<Recipe>) {
        val prefs = context.getSharedPreferences("daily_recipe_prefs", Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()

        val recipeJson = Gson().toJson(recipes)
        prefs.edit()
            .putString("shuffled_recipes", recipeJson)
            .putString("shuffle_date", today)
            .apply()
    }

    fun getShuffledDailyRecipes(context: Context): List<Recipe>? {
        val prefs = context.getSharedPreferences("daily_recipe_prefs", Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()
        val savedDate = prefs.getString("shuffle_date", "")

        return if (savedDate == today) {
            val json = prefs.getString("shuffled_recipes", null)
            if (json != null) {
                try {
                    val type = object : TypeToken<List<Recipe>>() {}.type
                    Gson().fromJson(json, type)
                } catch (e: Exception) {
                    null
                }
            } else null
        } else {
            null
        }
    }

    suspend fun syncBookmarkedFromFirestore() {
        try {
            val recipeRef = firestore.collection("recipes")
            val userDoc = firestore
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) return

            val bookmarkedIDs = userDoc.get("bookmarkedRecipeIDs") as? List<*>

            if (bookmarkedIDs.isNullOrEmpty()) return

            bookmarkedIDs.chunked(10).forEach { batchID ->
                val doc = recipeRef
                    .whereIn("id", batchID)
                    .get()
                    .await()

                val recipeFS = doc.documents.mapNotNull {
                    it.toObject(RecipeFS::class.java)
                }

                recipeFS.forEach{ recipe ->
                    dao.insertRecipe(recipe.toRecipeEntity(true))
                }
            }
        } catch (e: Exception) {
            Log.e("BookmarkSync", "Error sync from Firestore: ${e.message}")
        }
    }
}