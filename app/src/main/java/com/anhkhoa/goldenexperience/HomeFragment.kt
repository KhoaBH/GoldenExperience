package com.anhkhoa.goldenexperience

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.anhkhoa.goldenexperience.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var txtName: TextView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Khởi tạo view
        txtName = view.findViewById(R.id.welcome_title)

        // Load thông tin người dùng
        loadUserInfo(view)

        // Set click listener cho các card trong HomeFragment
        view.findViewById<View>(R.id.weightCard)?.setOnClickListener {
            loadFragment(WeightFragment())
        }

//        view.findViewById<View>(R.id.focusCard)?.setOnClickListener {
//            loadFragment(FocusFragment())
//        }
//
        view.findViewById<View>(R.id.mealCard)?.setOnClickListener {
            loadFragment(MealPrepFragment())
        }
//
//        view.findViewById<View>(R.id.screenTimeCard)?.setOnClickListener {
//            loadFragment(ScreenTimeFragment())
//        }

        return view
    }

    private fun loadUserInfo(view: View) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentUser = documentSnapshot.toObject(User::class.java)
                    if (currentUser != null) {
                        txtName.text = "Hello, ${currentUser.name}"
                    } else {
                        txtName.text = "Hello, Guest"
                    }
                } else {
                    txtName.text = "Hello, Guest"
                }
            }
            .addOnFailureListener {
                txtName.text = "Error loading user info"
            }
    }


    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
