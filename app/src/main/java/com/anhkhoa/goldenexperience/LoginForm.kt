package com.anhkhoa.goldenexperience

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginForm : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_form)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val rememberCheck = findViewById<CheckBox>(R.id.remember)
        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupLink = findViewById<TextView>(R.id.signupText)
        val remember = prefs.getBoolean("remember", false)
        if (remember) {
            usernameField.setText(prefs.getString("username", ""))
            passwordField.setText(prefs.getString("password", ""))
            rememberCheck.isChecked = true
        }
        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
                if (rememberCheck.isChecked) {
                    prefs.edit()
                        .putString("username", username)
                        .putString("password", password)
                        .putBoolean("remember", true)
                        .apply()
                } else {
                    prefs.edit()
                        .clear()
                        .apply()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signupLink.setOnClickListener {
            // Chuyển sang màn hình đăng ký (chưa có)
            val intent = Intent(this, RegisterForm::class.java)
            startActivity(intent)
        }
    }
    private fun loginUser(username: String, password: String) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Đăng nhập thành công
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Lấy UID của người dùng đã đăng nhập
                    val uid = auth.currentUser?.uid

                    if (uid != null) {
                        // Kiểm tra xem người dùng đã có thông tin trong Firestore chưa
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // Người dùng đã có thông tin, chuyển sang MainActivity
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    // Người dùng chưa có thông tin, chuyển sang UserInfoActivity
                                    val intent = Intent(this, UserInfoActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                            .addOnFailureListener {
                                // Lỗi khi lấy dữ liệu từ Firestore
                                Toast.makeText(this, "Failed to check user information.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Đăng nhập thất bại
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }


}
