package com.anhkhoa.goldenexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginForm : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_form)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // --- Khởi tạo Google Sign-In ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleLoginLayout = findViewById<LinearLayout>(R.id.googleLoginLayout)
        googleLoginLayout.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // --- Login bằng email/password như cũ ---
        val rememberCheck = findViewById<CheckBox>(R.id.remember)
        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupLink = findViewById<TextView>(R.id.signupText)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgetPassForm::class.java)
            startActivity(intent)
        }


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
                    prefs.edit().clear().apply()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signupLink.setOnClickListener {
            val intent = Intent(this, RegisterForm::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                val intent = if (document.exists()) {
                                    Intent(this, MainActivity::class.java)
                                } else {
                                    Intent(this, UserInfoActivity::class.java)
                                }
                                startActivity(intent)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to check user information.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- Xử lý kết quả đăng nhập Google ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                val intent = if (document.exists()) {
                                    Intent(this, MainActivity::class.java)
                                } else {
                                    Intent(this, UserInfoActivity::class.java)
                                }
                                startActivity(intent)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to check user information.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Firebase authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
