package com.anhkhoa.goldenexperience

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.anhkhoa.goldenexperience.models.User
import com.anhkhoa.goldenexperience.repository.WeightRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtAge: EditText
    private lateinit var edtHeight: EditText
    private lateinit var edtWeight: EditText
    private lateinit var edtTarget: EditText
    private lateinit var btnSave: Button
    private lateinit var edtDOB: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var activitySpinner: Spinner
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val weightRepository = WeightRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // Gán View
        edtName = findViewById(R.id.edtName)
        edtAge = findViewById(R.id.edtAge)
        edtHeight = findViewById(R.id.edtHeight)
        edtWeight = findViewById(R.id.edtWeight)
        btnSave = findViewById(R.id.btnSaveInfo)
        edtTarget = findViewById(R.id.edtTarget)
        genderSpinner = findViewById(R.id.spnGender)
        activitySpinner = findViewById(R.id.spnActivityLevel)
        edtDOB = findViewById(R.id.edtDateOfBirth)
        // Gắn dữ liệu cho Spinner
        setupSpinner(genderSpinner, R.array.gender_options)
        setupSpinner(activitySpinner, R.array.activity_levels)
        loadUserInfo()
        btnSave.setOnClickListener {
            saveUserInfo()
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        edtName.setText(it.name)
                        edtAge.setText(it.age.toString())
                        edtHeight.setText(it.height.toString())
                        edtWeight.setText(it.latestWeight.toString())
                        edtTarget.setText(it.target.toString())
                        edtDOB.setText(it.dob.toString())
                        // Set selected item cho gender spinner
                        val genderIndex = (genderSpinner.adapter as ArrayAdapter<String>)
                            .getPosition(it.gender)
                        genderSpinner.setSelection(genderIndex)

                        // Set selected item cho activity level spinner
                        val activityIndex = (activitySpinner.adapter as ArrayAdapter<String>)
                            .getPosition(it.activityLevel)
                        activitySpinner.setSelection(activityIndex)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show()
            }
    }
    private fun saveUserInfo() {
        val name = edtName.text.toString().trim()
        val age = edtAge.text.toString().toIntOrNull() ?: 0
        val dob = edtDOB.text.toString()
        val height = edtHeight.text.toString().toDoubleOrNull() ?: 0.0
        val weight = edtWeight.text.toString().toDoubleOrNull() ?: 0.0
        val target = edtTarget.text.toString().toDoubleOrNull() ?: 0.0
        if (name.isEmpty() || age == 0 || height == 0.0 || weight == 0.0) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = genderSpinner.selectedItem.toString()
        val activityLevel = activitySpinner.selectedItem.toString()


        val user = User(
            name = name,
            age = age,
            gender = gender,
            height = height,
            latestWeight = weight,
            activityLevel = activityLevel,
            target = target,
            dob = dob
        )

        val uid = auth.currentUser?.uid ?: return

        // Cập nhật tên người dùng trong Firebase Auth
        auth.currentUser?.updateProfile(
            userProfileChangeRequest {
                displayName = name
            }
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Lưu user info vào Firestore
                db.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        // Sau khi lưu user thành công, lưu luôn cân nặng
                        weightRepository.saveWeight(
                            weight,
                            onSuccess = {
                                Toast.makeText(this, "Lưu thông tin & cân nặng thành công!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            },
                            onFailure = { e ->
                                Toast.makeText(this, "Lưu thông tin thành công nhưng lỗi khi lưu cân nặng: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi khi lưu thông tin người dùng", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật tên người dùng", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
