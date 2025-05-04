package com.example.hibyassistant;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends BaseActivity {

    private TextInputLayout tilEmailForgetPassword;
    private EditText etForgotPasswordEmail;
    private AppCompatButton btnForgotPasswordSubmit;
    private TextView tvSubmitMsg;
    
    // Firebase instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);

        // Initialize Firebase instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        
        // Set click listeners
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        try {
            tilEmailForgetPassword = findViewById(R.id.tilEmailForgetPassword);
            etForgotPasswordEmail = findViewById(R.id.etForgotPasswordEmail);
            btnForgotPasswordSubmit = findViewById(R.id.btnForgotPasswordSubmit);
            tvSubmitMsg = findViewById(R.id.tvSubmitMsg);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        // Reset Password button click listener
        btnForgotPasswordSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (validateInputs()) {
                        showProgress("Sending reset link...");
                        resetPassword();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hideProgress();
                    Toast.makeText(ForgetPasswordActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String email = etForgotPasswordEmail.getText().toString().trim();

        if (email.isEmpty()) {
            tilEmailForgetPassword.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmailForgetPassword.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmailForgetPassword.setError(null);
        }

        return isValid;
    }

    private void resetPassword() {
        String email = etForgotPasswordEmail.getText().toString().trim();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        hideProgress();
                        tvSubmitMsg.setVisibility(View.VISIBLE);
                        showMessage("Password reset link sent to your email");
                        // Clear the email field
                        etForgotPasswordEmail.setText("");
                        // Go back to SignInActivity after a short delay
                        etForgotPasswordEmail.postDelayed(() -> finish(), 2000);
                    } else {
                        hideProgress();
                        tvSubmitMsg.setVisibility(View.GONE);
                        showMessage("Failed to send reset link: " + task.getException().getMessage());
                    }
                });
    }

    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}