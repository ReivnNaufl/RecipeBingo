package com.unluckygbs.recipebingo.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.dao.DailyEatsDao
import com.unluckygbs.recipebingo.data.dao.RecipeDao
import com.unluckygbs.recipebingo.data.dataclass.DailyEatsFS
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import com.unluckygbs.recipebingo.data.dataclass.RecipeFS
import com.unluckygbs.recipebingo.data.entity.DailyEatsEntity
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.DailyRecipeCrossRef
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import com.unluckygbs.recipebingo.data.extractIds
import com.unluckygbs.recipebingo.data.toDailyEatsEntity
import com.unluckygbs.recipebingo.data.toDailyEatsFS
import com.unluckygbs.recipebingo.data.toDailyRecipeCrossRefs
import com.unluckygbs.recipebingo.data.toRecipeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyEatsRepository(
    private val dailyEatsDao: DailyEatsDao,
    private val recipeDao: RecipeDao,
    private val firestore: FirebaseFirestore,
    private val userId: String

) {
    fun getLocalDailyEats(): Flow<List<DailyEatsWithRecipes>> = dailyEatsDao.getAllDailyEatsWithRecipes()

    suspend fun insertSingleRecipe(recipe: RecipeEntity) {
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val dailyEntry = dailyEatsDao.getDailyEatsWithRecipes(todayDate)

        if (dailyEntry == null) {
            // No entry for today - create new with recipe's nutrition
            dailyEatsDao.insertDailyEats(
                DailyEatsEntity(
                    date = todayDate,
                    totalNutrition = recipe.nutrition
                )
            )
            dailyEatsDao.insertCrossRef(
                DailyRecipeCrossRef(
                    date = todayDate,
                    id = recipe.id,
                    amount = 1
                )
            )
        } else {
            // Entry exists - handle recipe addition
            if (dailyEatsDao.isRecipeAlreadyAdded(todayDate, recipe.id)) {
                dailyEatsDao.recipeAmountPlusOne(todayDate, recipe.id)
            } else {
                dailyEatsDao.insertCrossRef(
                    DailyRecipeCrossRef(
                        date = todayDate,
                        id = recipe.id,
                        amount = 1
                    )
                )
            }

            // Get CURRENT nutrition and merge with new recipe's nutrition
            val currentNutrition = dailyEntry.dailyEats.totalNutrition ?: emptyList()
            val mergedNutrition = mergeNutrients(currentNutrition, recipe.nutrition)

            // Update with merged values
            dailyEatsDao.updateDailyEatsNutrition(todayDate, mergedNutrition)
        }
        syncSingleDailyEatsToFirestore(date = todayDate)
    }

    private fun mergeNutrients(list1: List<Nutrient>?, list2: List<Nutrient>): List<Nutrient> {
        val mergedMap = mutableMapOf<String, Nutrient>()

        if (list1.isNullOrEmpty()){
            return list2
        }

        // Process first list
        list1.forEach { nutrient ->
            mergedMap[nutrient.name] = nutrient
        }

        // Process second list - add amounts if name exists
        list2.forEach { nutrient ->
            mergedMap.merge(nutrient.name, nutrient) { existing, new ->
                existing.copy(amount = existing.amount + new.amount)
            }
        }

        return mergedMap.values.toList()
    }

    suspend fun deleteDailyEats(dailyEats: DailyEatsEntity){
        dailyEatsDao.deleteDailyEats(dailyEats)
    }

    suspend fun getDailyEatsWithRecipes(date: String): DailyEatsWithRecipes?{
        return dailyEatsDao.getDailyEatsWithRecipes(date)
    }

    suspend fun clearAll() = dailyEatsDao.clearAll()

    private suspend fun syncSingleDailyEatsToFirestore(date: String) {
        try {
            val dailyEatsWithRecipes = dailyEatsDao.getDailyEatsWithRecipes(date) ?: return

            val ref = firestore.collection("users")
                .document(userId)
                .collection("Daily_Eats")
                .document(date)

            val crossRefs = dailyEatsDao.getDateCrossRef(dailyEatsWithRecipes.dailyEats.date)

            ref.set(dailyEatsWithRecipes.toDailyEatsFS(crossRefs)).await()
            Log.d("SyncDailyEats", "Daily Eats ${dailyEatsWithRecipes.dailyEats.date} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("SyncDailyEats", "Failed to sync daily eats: ${e.message}")
            Log.d("SyncDailyEatsDebug", "userId: $userId, date: $date")
        }
    }

    suspend fun syncDailyEatsFromFirestore() {
        try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("Daily_Eats")
                .get()
                .await()

            val dailyFS = snapshot.documents.mapNotNull { document ->
                document.toObject(DailyEatsFS::class.java)
            }

            dailyFS.forEach { singleFS ->
                val dailyEatsEntity = singleFS.toDailyEatsEntity()
                val crossRefs = singleFS.toDailyRecipeCrossRefs()

                dailyEatsDao.insertDailyEats(dailyEatsEntity)

                crossRefs.chunked(10).forEach { crossRef10->
                    val recipeIDs = crossRef10.extractIds()
                    val doc = firestore
                        .collection("recipes")
                        .whereIn(FieldPath.documentId(), recipeIDs)
                        .get()
                        .await()

                    val recipeFS = doc.documents.mapNotNull {
                        it.toObject(RecipeFS::class.java)
                    }


                    Log.d("DailyEatsSyncDebug", "${recipeIDs}")
                    Log.d("DailyEatsSyncDebug", "${doc.documents}")
                    Log.d("DailyEatsSyncDebug", "${recipeFS}")

                    recipeFS.forEach { recipe ->
                        if (!recipeDao.isRecipeExist(recipe.id)){
                            recipeDao.insertRecipe(recipe.toRecipeEntity(false))
                        }
                    }

                    crossRef10.forEach { crossRef ->
                        dailyEatsDao.insertCrossRef(crossRef)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DailyEatsSync", "Error sync from Firestore: ${e.message}")
        }
    }
}