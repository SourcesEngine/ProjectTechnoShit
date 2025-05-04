package com.example.hibyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import com.example.hibyassistant.utils.NetworkUtils;

public class SignInActivity extends BaseActivity {

    // UI Components
    private TextInputLayout tilEmail, tilPassword;
    private EditText etSignInEmail, etSignInPassword;
    private AppCompatButton btnSignIn, btnGoogleSignIn;
    private TextView tvSignUpPage, tvForgetPassword;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ActivityResultLauncher for Google Sign-In
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                });

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        try {
            tilEmail = findViewById(R.id.tilEmail);
            tilPassword = findViewById(R.id.tilPassword);
            etSignInEmail = findViewById(R.id.etSignInEmail);
            etSignInPassword = findViewById(R.id.etSignInPassword);
            btnSignIn = findViewById(R.id.btnSignIn);
            btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
            tvSignUpPage = findViewById(R.id.tvSignUpPage);
            tvForgetPassword = findViewById(R.id.tvForgetPassword);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error initializing views: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> {
            try {
                if (validateInputs()) {
                    signInUser();
                }
            } catch (Exception e) {
                showMessage("Error: " + e.getMessage());
            }
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            try {
                signInWithGoogle();
            } catch (Exception e) {
                showMessage("Error: " + e.getMessage());
            }
        });

        tvSignUpPage.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        tvForgetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String email = etSignInEmail.getText().toString().trim();
        String password = etSignInPassword.getText().toString().trim();

        // Email validation
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Password validation
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }

    private void signInWithGoogle() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showMessage("No internet connection. Please check your connection and try again.");
            return;
        }

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getEmail() != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                showMessage("Google sign in failed");
            }
        } catch (ApiException e) {
            showMessage("Google sign in failed: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update last login time
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("lastLogin", System.currentTimeMillis());

                            db.collection("users").document(user.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        showMessage("Sign in successful!");
                                        // Navigate directly to MainActivity
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        showMessage("Error updating user data: " + e.getMessage());
                                        // Still navigate to MainActivity even if update fails
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        showMessage("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void signInUser() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showMessage("No internet connection. Please check your connection and try again.");
            return;
        }

        String email = etSignInEmail.getText().toString().trim();
        String password = etSignInPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update last login time
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("lastLogin", System.currentTimeMillis());

                            db.collection("users").document(user.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        showMessage("Sign in successful!");
                                        // Navigate directly to MainActivity
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        showMessage("Error updating user data: " + e.getMessage());
                                        // Still navigate to MainActivity even if update fails
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        handleSignInError(task.getException());
                    }
                });
    }

    private void handleSignInError(Exception e) {
        String errorMessage = "Sign in failed";
        if (e != null) {
            String errorCode = e.getMessage();
            if (errorCode.contains("user-not-found")) {
                errorMessage = "No account found with this email";
            } else if (errorCode.contains("wrong-password")) {
                errorMessage = "Incorrect password";
            } else if (errorCode.contains("invalid-email")) {
                errorMessage = "Invalid email format";
            } else if (errorCode.contains("user-disabled")) {
                errorMessage = "This account has been disabled";
            } else {
                errorMessage = e.getMessage();
            }
        }
        showMessage(errorMessage);
    }

    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}