package com.anhkhoa.goldenexperience.models

import java.io.Serializable

data class Meal(
    val name: String = "",
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val type: String = "",
    val unit: String = "",
    val imageUrl: String = "",
    val description: String = ""
) : Serializable



