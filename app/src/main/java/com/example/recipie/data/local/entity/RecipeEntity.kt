package com.example.recipie.data.local.entity

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

    val name: String?,
    val url: String?,
    val category: String?,
    val author: String?,
    val summary: String?,

    val rating: Double?,
    val rating_count: Double?,
    val review_count: Double?,

    val ingredients: String?,
    val instructions: String?,

    val prep: String?,
    val cook: String?,
    val total: String?,
    val servings: Double?,
    val yield: String?,

    val calories: Double?,
    val carbohydrates_g: Double?,
    val sugars_g: Double?,
    val fat_g: Double?,
    val saturated_fat_g: Double?,
    val cholesterol_mg: Double?,
    val protein_g: Double?,
    val dietary_fiber_g: Double?,
    val sodium_mg: Double?,
    val calories_from_fat: Double?,
    val calcium_mg: Double?,
    val iron_mg: Double?,
    val magnesium_mg: Double?,
    val potassium_mg: Double?,
    val zinc_mg: Double?,
    val phosphorus_mg: Double?,
    val vitamin_a_iu_IU: Double?,
    val niacin_equivalents_mg: Double?,
    val vitamin_b6_mg: Double?,
    val vitamin_c_mg: Double?,
    val folate_mcg: Double?,
    val thiamin_mg: Double?,
    val riboflavin_mg: Double?,
    val vitamin_e_iu_IU: Double?,
    val vitamin_k_mcg: Double?,
    val biotin_mcg: Double?,
    val vitamin_b12_mcg: Double?,
    val mono_fat_g: Double?,
    val poly_fat_g: Double?,
    val trans_fatty_acid_g: Double?,
    val omega_3_fatty_acid_g: Double?,
    val omega_6_fatty_acid_g: Double?
)
