package com.unluckygbs.recipebingo.repository

import com.unluckygbs.recipebingo.data.dao.IngredientDao
import com.unluckygbs.recipebingo.data.entity.IngredientEntity

class IngredientRepository(private val dao: IngredientDao) {
    val allIngredients = dao.getAllIngredients()

    suspend fun insert(ingredient: IngredientEntity) = dao.insertIngredient(ingredient)

    suspend fun delete(ingredient: IngredientEntity) = dao.deleteIngredient(ingredient)

    suspend fun update(ingredient: IngredientEntity) = dao.updateIngredient(ingredient)

    suspend fun clearAll() = dao.clearAll()


}
