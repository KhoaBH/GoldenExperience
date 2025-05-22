package com.anhkhoa.goldenexperience.repository

import android.util.Log
import com.anhkhoa.goldenexperience.models.Weight
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeightRepository {

    private val db = FirebaseFirestore.getInstance()
    private val weightCollection = db.collection("weights")
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun saveWeight(weight: Double, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = uid ?: return

        val weightData = Weight(
            weight = weight,
            timestamp = Timestamp.now(),
            userId = userId
        )

        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)

        deleteTodayWeights {
            // Thêm vào collection "weight"
            weightCollection.add(weightData.toMap())
                .addOnSuccessListener {
                    // Update latestWeight vào document user
                    userDocRef.update("latestWeight", weight)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure(e) }
                }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun Weight.toMap(): Map<String, Any> = mapOf(
        "weight" to this.weight,
        "timestamp" to this.timestamp,
        "userId" to this.userId
    )

    private fun deleteTodayWeights(onComplete: () -> Unit) {
        val currentDate = Timestamp.now().toDate()
        val calendar = java.util.Calendar.getInstance().apply {
            time = currentDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.time

        weightCollection
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", currentDate)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    weightCollection.document(doc.id).delete()
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("WeightRepository", "Failed to delete today's weights: ${e.message}")
                onComplete() // Vẫn tiếp tục thêm dù có lỗi xoá
            }
    }

    fun getLatestWeight(onResult: (Double?) -> Unit) {
        weightCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val weight = result.documents.firstOrNull()?.getDouble("weight")
                onResult(weight)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getAllWeights(onResult: (List<Weight>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = uid ?: return

        weightCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val weights = documents.mapNotNull { doc ->
                    doc.toObject(Weight::class.java)
                }
                onResult(weights)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}


