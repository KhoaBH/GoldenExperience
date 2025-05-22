package com.anhkhoa.goldenexperience.models

import com.google.firebase.Timestamp

data class Weight(
    val weight: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = ""
)

