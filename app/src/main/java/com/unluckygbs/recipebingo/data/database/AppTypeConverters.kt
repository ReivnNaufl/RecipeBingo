package com.unluckygbs.recipebingo.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.unluckygbs.recipebingo.data.dataclass.AnalyzedInstruction
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import com.unluckygbs.recipebingo.data.dataclass.RecipeIngredient
import com.unluckygbs.recipebingo.data.entity.RecipeEntity

class AppTypeConverters {
    // RecipeIngredient
    @TypeConverter
    fun fromRecipeIngredientList(value: List<RecipeIngredient>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toRecipeIngredientList(value: String?): List<RecipeIngredient>? {
        return value?.let {
            val type = object : TypeToken<List<RecipeIngredient>>() {}.type
            Gson().fromJson(it, type)
        }
    }

    // RecipeEntity
    @TypeConverter
    fun fromRecipeEntityList(value: List<RecipeEntity>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toRecipeEntityList(value: String?): List<RecipeEntity>? {
        return value?.let {
            val type = object : TypeToken<List<RecipeEntity>>() {}.type
            Gson().fromJson(it, type)
        }
    }

    // Nutrient
    @TypeConverter
    fun fromNutrientList(value: List<Nutrient>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toNutrientList(value: String?): List<Nutrient>? {
        return value?.let {
            val type = object : TypeToken<List<Nutrient>>() {}.type
            Gson().fromJson(it, type)
        }
    }

    // AnalyzedInstruction
    @TypeConverter
    fun fromAnalyzedInstructionList(value: List<AnalyzedInstruction>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toAnalyzedInstructionList(value: String?): List<AnalyzedInstruction>? {
        return value?.let {
            val type = object : TypeToken<List<AnalyzedInstruction>>() {}.type
            Gson().fromJson(it, type)
        }
    }
}