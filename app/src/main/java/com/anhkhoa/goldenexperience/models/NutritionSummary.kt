package com.anhkhoa.goldenexperience.models

data class NutritionSummary(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

fun calculateNutrition(mealPlans: List<MealPlan>): NutritionSummary {
    var totalCalories = 0
    var totalProtein = 0f
    var totalCarbs = 0f
    var totalFat = 0f

    for (plan in mealPlans) {
        for (mealInPlan in plan.meals) {
            val factor = mealInPlan.quantity
            val meal = mealInPlan.meal
            totalCalories += (meal.calories * factor).toInt()
            totalProtein += meal.protein * factor
            totalCarbs += meal.carbs * factor
            totalFat += meal.fat * factor
        }
    }

    return NutritionSummary(
        calories = totalCalories,
        protein = totalProtein,
        carbs = totalCarbs,
        fat = totalFat
    )
}