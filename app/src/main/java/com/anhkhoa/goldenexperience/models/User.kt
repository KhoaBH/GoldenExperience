package com.anhkhoa.goldenexperience.models

data class User(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Double = 0.0,
    val latestWeight: Double = 0.0,
    val activityLevel: String = "",     // Ví dụ: Low / Medium / High
    val target: Double = 0.0,           // Ví dụ: Giảm cân / Giữ cân / Tăng cân
)
