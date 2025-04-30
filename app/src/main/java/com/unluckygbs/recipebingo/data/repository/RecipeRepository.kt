package com.unluckygbs.recipebingo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.dao.DailyEatsDao
import com.unluckygbs.recipebingo.data.dao.RecipeDao
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val dao: RecipeDao,
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    suspend fun getLocalDailyEats(): Flow<List<RecipeEntity>> = dao.getAll()

    suspend fun insertSingleRecipe(recipeEntity: RecipeEntity) {
        dao.insertRecipe(recipeEntity)
    }

    suspend fun deleteAll(){
        dao.clearAll()
    }

    suspend fun isRecipeExist(id: Int): Boolean{
        return dao.isRecipeExist(id)
    }
}