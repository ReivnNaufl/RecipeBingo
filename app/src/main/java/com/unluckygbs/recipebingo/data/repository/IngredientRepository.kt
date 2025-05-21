package com.unluckygbs.recipebingo.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.client.KeyClient
import com.unluckygbs.recipebingo.data.client.SpoonacularClient
import com.unluckygbs.recipebingo.data.dao.IngredientDao
import com.unluckygbs.recipebingo.data.dataclass.Ingredient
import com.unluckygbs.recipebingo.data.dataclass.RecipeIngredient
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

            dao.insertAll(ingredients)
        } catch (e: Exception) {
            Log.e("IngredientsSync", "Error sync from Firestore: ${e.message}")
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

    suspend fun subtractIngredientv1(extendedIngredient: List<RecipeIngredient>): Pair<Boolean, String> {
        val inventory: List<IngredientEntity> = dao.getAllAsList()
        var inventoryChanges: MutableList<IngredientEntity> = mutableListOf()

        if (extendedIngredient.size > inventory.size) {
            return Pair(false, "Not enough Ingredient")
        }

        extendedIngredient.forEach { ingredient ->
//            If ingredient found in inventory
            if (inventory.any { it.name == ingredient.name }) {
                val foundIngredient = inventory.find { it.name == ingredient.name }

//                if ingredient have matching unit
                if (foundIngredient?.unit == ingredient.measures.metric.unitLong) {
//                    if ingredient in inventory is enough
                    if (foundIngredient.quantity > ingredient.measures.metric.amount) {
                        val newIngredient =
                            foundIngredient.copy(quantity = foundIngredient.quantity - ingredient.measures.metric.amount)
                        inventoryChanges.add(newIngredient)
                    } else {
                        return Pair(false, "Not enough ${ingredient.name} to make this recipe")
                    }
                } else {
                    val key = KeyClient.apiService.getapikey(type = "DEFAULT")
                    val response = SpoonacularClient.apiService.convertIngredientAmount(
                        apiKey = key.key,
                        ingredientName = ingredient.name,
                        sourceAmount = ingredient.measures.metric.amount,
                        sourceUnit = ingredient.measures.metric.unitLong,
                        targetUnit = foundIngredient?.unit
                    )

                    if (foundIngredient != null) {
//                        if ingredient in inventory is enough
                        if (foundIngredient.quantity > response.targetAmount) {
                            val newIngredient =
                                foundIngredient.copy(quantity = foundIngredient.quantity - response.targetAmount)
                            inventoryChanges.add(newIngredient)
                        } else {
                            return Pair(false, "Not enough ${ingredient.name} to make this recipe")
                        }
                    }
                }
            }
        }

        inventoryChanges.forEach { ingredient ->
            dao.updateIngredient(ingredient)
        }

        return Pair(true, "Inventory subtraction is successful")
    }

    suspend fun subtractIngredient(recipeIngredients: List<RecipeIngredient>): Pair<Boolean, String> {
        val inventory = dao.getAllAsList()
        val inventoryChanges = mutableListOf<IngredientEntity>()

        // Early check if we have all required ingredients
        val missingIngredients = recipeIngredients.filterNot { recipeIngredient ->
            inventory.any { it.name == recipeIngredient.name }
        }
        if (missingIngredients.isNotEmpty()) {
            return Pair(false, "Missing ingredients: ${missingIngredients.joinToString { it.name }}")
        }

        for (recipeIngredient in recipeIngredients) {
            val inventoryIngredient = inventory.first { it.name == recipeIngredient.name }
            val requiredAmount = recipeIngredient.measures.metric.amount
            val requiredUnit = recipeIngredient.measures.metric.unitLong

            try {
                val amountToSubtract = if (inventoryIngredient.unit == requiredUnit) {
                    requiredAmount
                } else {
                    // Convert units if they don't match
                    val key = KeyClient.apiService.getapikey(type = "DEFAULT")
                    SpoonacularClient.apiService.convertIngredientAmount(
                        apiKey = key.key,
                        ingredientName = recipeIngredient.name,
                        sourceAmount = requiredAmount,
                        sourceUnit = requiredUnit,
                        targetUnit = inventoryIngredient.unit
                    ).targetAmount
                }

                if (inventoryIngredient.quantity < amountToSubtract) {
                    return Pair(false, "Not enough ${recipeIngredient.name} (need $amountToSubtract ${inventoryIngredient.unit})")
                }

                inventoryChanges.add(
                    inventoryIngredient.copy(quantity = inventoryIngredient.quantity - amountToSubtract)
                )
            } catch (e: Exception) {
                return Pair(false, "Failed to process ${recipeIngredient.name}: ${e.message}")
            }
        }

        inventoryChanges.forEach { dao.updateIngredient(it) }

        return Pair(true, "Successfully subtracted ingredients")
    }
}


