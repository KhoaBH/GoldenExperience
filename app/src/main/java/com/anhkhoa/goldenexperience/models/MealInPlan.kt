package com.anhkhoa.goldenexperience.models

data class MealInPlan(
    val meal: Meal = Meal(),
    val quantity: Float = 1f,
    val unit: String = "100g"
)