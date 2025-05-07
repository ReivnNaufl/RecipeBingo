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

    fun getAllRecipe(): Flow<List<RecipeEntity>> = dao.getAll()

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
}