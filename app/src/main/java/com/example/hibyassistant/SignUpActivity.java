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

public class SignUpActivity extends BaseActivity {

    // UI Components
    private TextInputLayout tilName, tilEmail, tilPassword;
    private EditText etSinUpName, etSinUpEmail, etSinUpPassword;
    private AppCompatButton btnSignUp, btnGoogleSignUp;
    private TextView tvLoginPage;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Check internet connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showMessage("No internet connection. Please check your connection and try again.");
            return;
        }

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

    /**
     * Initializes all UI components by finding their views
     * Handles any initialization errors gracefully
     */
    private void initializeViews() {
        try {
            tilName = findViewById(R.id.tilName);
            tilEmail = findViewById(R.id.tilEmail);
            tilPassword = findViewById(R.id.tilPassword);
            etSinUpName = findViewById(R.id.etSinUpName);
            etSinUpEmail = findViewById(R.id.etSinUpEmail);
            etSinUpPassword = findViewById(R.id.etSinUpPassword);
            btnSignUp = findViewById(R.id.btnSignUp);
            btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
            tvLoginPage = findViewById(R.id.tvLoginPage);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sets up click listeners for all interactive UI elements
     * Handles navigation and user actions
     */
    private void setupClickListeners() {
        // Sign Up button click listener
        btnSignUp.setOnClickListener(v -> {
            try {
                if (validateInputs()) {
                    registerUser();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Error: " + e.getMessage());
            }
        });

        // Google Sign Up button click listener
        btnGoogleSignUp.setOnClickListener(v -> {
            try {
                signInWithGoogle();
            } catch (Exception e) {
                showMessage("Error: " + e.getMessage());
            }
        });

        // Login text click listener
        tvLoginPage.setOnClickListener(v -> {
            try {
                finish(); // Go back to SignInActivity
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Validates all input fields
     * Checks for empty fields, valid email format, and password requirements
     * @return boolean indicating if all inputs are valid
     */
    private boolean validateInputs() {
        boolean isValid = true;
        String name = etSinUpName.getText().toString().trim();
        String email = etSinUpEmail.getText().toString().trim();
        String password = etSinUpPassword.getText().toString().trim();

        // Validate name
        if (name.isEmpty()) {
            tilName.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            tilName.setError("Name must be at least 2 characters");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        // Validate email
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!email.toLowerCase().endsWith("@gmail.com")) {
            tilEmail.setError("Please use a Gmail account");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            tilPassword.setError("Password must contain at least one uppercase letter");
            isValid = false;
        } else if (!password.matches(".*[a-z].*")) {
            tilPassword.setError("Password must contain at least one lowercase letter");
            isValid = false;
        } else if (!password.matches(".*\\d.*")) {
            tilPassword.setError("Password must contain at least one number");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }

    /**
     * Handles the user registration process
     * Creates user account in Firebase Authentication
     * Stores additional user data in Firestore
     */
    private void registerUser() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showMessage("No internet connection. Please check your connection and try again.");
            return;
        }

        String name = etSinUpName.getText().toString().trim();
        String email = etSinUpEmail.getText().toString().trim();
        String password = etSinUpPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("uid", user.getUid());
                            userData.put("createdAt", System.currentTimeMillis());
                            userData.put("lastLogin", System.currentTimeMillis());
                            userData.put("isGoogleSignIn", false);

                            db.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        showMessage("Registration successful!");
                                        // Sign out the user to force them to sign in again
                                        mAuth.signOut();
                                        // Navigate directly to SignInActivity
                                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        user.delete();
                                        showMessage("Error saving user data: " + e.getMessage());
                                    });
                        }
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
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
            if (account != null && account.getEmail() != null && account.getEmail().toLowerCase().endsWith("@gmail.com")) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                showMessage("Please use a Gmail account to sign up");
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
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", user.getDisplayName());
                            userData.put("email", user.getEmail());
                            userData.put("uid", user.getUid());
                            userData.put("createdAt", System.currentTimeMillis());
                            userData.put("lastLogin", System.currentTimeMillis());
                            userData.put("isGoogleSignIn", true);

                            db.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        showMessage("Registration successful!");
                                        // Sign out the user to force them to sign in again
                                        mAuth.signOut();
                                        mGoogleSignInClient.signOut();
                                        // Navigate directly to SignInActivity
                                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        user.delete();
                                        showMessage("Error saving user data: " + e.getMessage());
                                    });
                        }
                    } else {
                        showMessage("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void handleRegistrationError(Exception e) {
        String errorMessage = "Registration failed";
        if (e != null) {
            String errorCode = e.getMessage();
            if (errorCode.contains("email-already-in-use")) {
                errorMessage = "This email is already registered";
            } else if (errorCode.contains("weak-password")) {
                errorMessage = "Password is too weak";
            } else if (errorCode.contains("invalid-email")) {
                errorMessage = "Invalid email format";
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