package com.unluckygbs.recipebingo.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.dao.DailyEatsDao
import com.unluckygbs.recipebingo.data.dao.IngredientDao
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import com.unluckygbs.recipebingo.data.entity.DailyEatsEntity
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.DailyRecipeCrossRef
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import com.unluckygbs.recipebingo.data.toDailyEatsFS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class DailyEatsRepository(
    private val dao: DailyEatsDao,
    private val firestore: FirebaseFirestore,
    private val userId: String

) {
    fun getLocalDailyEats(): Flow<List<DailyEatsWithRecipes>> = dao.getAllDailyEatsWithRecipes()

    suspend fun insertSingleRecipe(recipe: RecipeEntity) {
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val dailyEntry = dao.getDailyEatsWithRecipes(todayDate)

        if (dailyEntry == null) {
            // No entry for today - create new with recipe's nutrition
            dao.insertDailyEats(
                DailyEatsEntity(
                    date = todayDate,
                    totalNutrition = recipe.nutrition
                )
            )
            dao.insertCrossRef(
                DailyRecipeCrossRef(
                    date = todayDate,
                    id = recipe.id,
                    amount = 1
                )
            )
        } else {
            // Entry exists - handle recipe addition
            if (dao.isRecipeAlreadyAdded(todayDate, recipe.id)) {
                dao.recipeAmountPlusOne(todayDate, recipe.id)
            } else {
                dao.insertCrossRef(
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
            dao.updateDailyEatsNutrition(todayDate, mergedNutrition)

            syncSingleDailyEatsToFirestore(date = todayDate)
        }
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
        dao.deleteDailyEats(dailyEats)
    }

    suspend fun getDailyEatsWithRecipes(date: String): DailyEatsWithRecipes?{
        return dao.getDailyEatsWithRecipes(date)
    }

    suspend fun clearAll() = dao.clearAll()

    private suspend fun syncSingleDailyEatsToFirestore(date: String) {
        try {
            val dailyEatsWithRecipes = dao.getDailyEatsWithRecipes(date) ?: return

            val ref = firestore.collection("users")
                .document(userId)
                .collection("Daily Eats")
                .document(date)

            val crossRefs = dao.getDateCrossRef(dailyEatsWithRecipes.dailyEats.date)

            ref.set(dailyEatsWithRecipes.toDailyEatsFS(crossRefs)).await()
            Log.d("SyncDailyEats", "Daily Eats ${dailyEatsWithRecipes.dailyEats.date} synced to Firestore.")
        } catch (e: Exception) {
            Log.e("SyncDailyEats", "Failed to sync daily eats: ${e.message}")
            Log.d("SyncDailyEatsDebug", "userId: $userId, date: $date")
        }
    }
}