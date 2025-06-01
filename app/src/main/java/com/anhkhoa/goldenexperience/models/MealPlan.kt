package com.anhkhoa.goldenexperience.models

data class MealPlan(
    val planId: String = "",
    val userId: String = "",
    val planName: String = "",
    val mealType: String = "",      // Loại bữa ăn, ví dụ "Breakfast"
    val meals: List<MealInPlan> = emptyList(),
    val createdAt: Long = 0L
)
