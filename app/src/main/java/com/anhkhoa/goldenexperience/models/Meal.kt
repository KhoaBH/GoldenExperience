package com.anhkhoa.goldenexperience.models

data class Meal(
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val type: String = "",
    val unit: String = "",
    val imageUrl: String = "",
    val description: String = ""
)


