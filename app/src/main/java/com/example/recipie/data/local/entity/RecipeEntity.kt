package com.example.recipie.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes",
    indices = [
        Index(value = ["name"], name = "index_recipes_name"),
        Index(value = ["category"], name = "index_recipes_category")
    ]
)
data class RecipeEntity(
    @PrimaryKey
    val id: Long,

    val name: String? = null,
    val url: String? = null,
    val category: String? = null,
    val author: String? = null,
    val summary: String? = null,

    val rating: Double? = null,
    val rating_count: Double? = null,
    val review_count: Double? = null,

    val ingredients: String? = null,
    val instructions: String? = null,

    val prep: String? = null,
    val cook: String? = null,
    val total: String? = null,
    val servings: Double? = null,
    val yield: String? = null,

    val calories: Double? = null,
    val carbohydrates_g: Double? = null,
    val sugars_g: Double? = null,
    val fat_g: Double? = null,
    val saturated_fat_g: Double? = null,
    val cholesterol_mg: Double? = null,
    val protein_g: Double? = null,
    val dietary_fiber_g: Double? = null,
    val sodium_mg: Double? = null,
    val calories_from_fat: Double? = null,
    val calcium_mg: Double? = null,
    val iron_mg: Double? = null,
    val magnesium_mg: Double? = null,
    val potassium_mg: Double? = null,
    val zinc_mg: Double? = null,
    val phosphorus_mg: Double? = null,
    val vitamin_a_iu_IU: Double? = null,
    val niacin_equivalents_mg: Double? = null,
    val vitamin_b6_mg: Double? = null,
    val vitamin_c_mg: Double? = null,
    val folate_mcg: Double? = null,
    val thiamin_mg: Double? = null,
    val riboflavin_mg: Double? = null,
    val vitamin_e_iu_IU: Double? = null,
    val vitamin_k_mcg: Double? = null,
    val biotin_mcg: Double? = null,
    val vitamin_b12_mcg: Double? = null,
    val mono_fat_g: Double? = null,
    val poly_fat_g: Double? = null,
    val trans_fatty_acid_g: Double? = null,
    val omega_3_fatty_acid_g: Double? = null,
    val omega_6_fatty_acid_g: Double? = null,
    @ColumnInfo(name = "original_name") val originalName: String? = null,
    @ColumnInfo(name = "name_search", defaultValue = "''") val nameSearch: String = "",
    @ColumnInfo(name = "summary_pt") val summaryPt: String? = null,
    @ColumnInfo(name = "ingredients_pt") val ingredientsPt: String? = null,
    @ColumnInfo(name = "instructions_pt") val instructionsPt: String? = null
)
