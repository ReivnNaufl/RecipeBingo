package com.unluckygbs.recipebingo.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.dao.IngredientDao
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class IngredientRepository(
    private val dao: IngredientDao,
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    fun getLocalIngredients(): Flow<List<IngredientEntity>> = dao.getAll()

    suspend fun syncFromFirestoreToRoom() {
        try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("Ingredients")
                .get()
                .await()

            val ingredients = snapshot.documents.mapNotNull {
                it.toObject(IngredientEntity::class.java)
            }

            dao.clearAll()
            dao.insertAll(ingredients)
        } catch (e: Exception) {
            Log.e("Sync", "Error sync from Firestore: ${e.message}")
        }
    }

    suspend fun syncFromRoomToFirestore() {
        try {
            val localIngredients = dao.getAllOnce()
            val ref = firestore.collection("users").document(userId).collection("Ingredients")

            localIngredients.forEach { ingredient ->
                ref.document(ingredient.id.toString()).set(ingredient).await()
            }
        } catch (e: Exception) {
            Log.e("Sync", "Error sync to Firestore: ${e.message}")
        }
    }

    suspend fun syncSingleIngredientToFirestore(ingredient: IngredientEntity) {
        try {
            val ref = firestore.collection("users")
                .document(userId)
                .collection("Ingredients")
                .document(ingredient.id.toString())

            ref.set(ingredient).await()
            Log.d("Sync", "Ingredient ${ingredient.name} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("Sync", "Failed to sync ingredient: ${e.message}")
            Log.d("SyncDebug", "userId: $userId, ingredient.id: ${ingredient.id}")
        }
    }

    suspend fun insertIngredient(ingredient: IngredientEntity) {
        dao.insertIngredient(ingredient)
        syncSingleIngredientToFirestore(ingredient)
    }

    suspend fun updateIngredientQuantity(ingredient: IngredientEntity) {
        dao.updateIngredient(ingredient)
        syncSingleIngredientToFirestore(ingredient)
    }

    suspend fun deleteIngredient(ingredient: IngredientEntity) {
        dao.deleteIngredient(ingredient)

        val userId = userId
        firestore.collection("users")
            .document(userId)
            .collection("Ingredients")
            .document(ingredient.id.toString())
            .delete()
            .await()
    }

    suspend fun getIncludeIngredientsQuery(): String {
        return dao.getAllOnce()
            .map { it.name.trim().lowercase().replace(" ", "%20") } // encode spasi
            .joinToString(",") // tidak perlu `+`
    }

    val allIngredients = dao.getAllIngredients()

    suspend fun insert(ingredient: IngredientEntity) = dao.insertIngredient(ingredient)

    suspend fun delete(ingredient: IngredientEntity) = dao.deleteIngredient(ingredient)

    suspend fun update(ingredient: IngredientEntity) = dao.updateIngredient(ingredient)

    suspend fun clearAll() = dao.clearAll()


}
