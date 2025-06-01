package com.anhkhoa.goldenexperience

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.anhkhoa.goldenexperience.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment() {

    private lateinit var tvFullName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvTarget: TextView
    private lateinit var tvActivity: TextView
    private lateinit var tvDOB: TextView
    private lateinit var btnEdit: Button
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)


        tvFullName = view.findViewById(R.id.tvFullName)
        tvAge = view.findViewById(R.id.tvAge)
        tvGender = view.findViewById(R.id.tvGender)
        tvHeight = view.findViewById(R.id.tvHeight)
        tvWeight = view.findViewById(R.id.tvWeight)
        tvTarget = view.findViewById(R.id.tvTarget)
        tvActivity = view.findViewById(R.id.tvActivity)
        tvDOB = view.findViewById(R.id.DOB)
        btnEdit = view.findViewById(R.id.btnEdit)

        loadUserProfile()
        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), UserInfoActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        loadUserProfile()
    }
    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    user?.let {
                        tvFullName.text = it.name
                        tvAge.text = "Age: ${it.age}"
                        tvGender.text = "Gender: ${it.gender}"
                        tvHeight.text = "Height: ${it.height} "
                        tvWeight.text = "Weight: ${it.latestWeight}"
                        tvTarget.text = "Target: ${it.target}"
                        tvDOB.text = "${it.dob}"
                        tvActivity.text = "Activity: ${it.activityLevel}"
                    }
                } else {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user info", Toast.LENGTH_SHORT).show()
            }
    }
}
