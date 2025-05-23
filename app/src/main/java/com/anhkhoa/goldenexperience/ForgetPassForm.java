package com.anhkhoa.goldenexperience;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassForm extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendButton;
    private TextView signInText;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass_form);

        // Ánh xạ view
        emailEditText = findViewById(R.id.emailEditText);
        sendButton = findViewById(R.id.registerButton);
        signInText = findViewById(R.id.signupText);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Xử lý khi bấm nút Send
        sendButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!email.isEmpty()) {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgetPassForm.this, "Password reset link sent to " + email, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ForgetPassForm.this, "Failed to send reset email. Please check your email address.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(ForgetPassForm.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý khi bấm "Sign in"
        signInText.setOnClickListener(v -> {
            Intent intent = new Intent(ForgetPassForm.this, LoginForm.class);
            startActivity(intent);
            finish();
        });
    }
}
