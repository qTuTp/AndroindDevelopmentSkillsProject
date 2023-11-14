package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;


public class RegisterPage extends AppCompatActivity {


    //Declare Variable
    private TextInputLayout name;
    private TextInputLayout matrix;
    private TextInputLayout email;
    private TextInputLayout phone;
    private TextInputLayout password;
    private TextInputLayout confirmPassword;
    private MaterialButton registerButton;
    private MaterialButton cancelButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        setReference();


    }

    private void setReference(){
        name = findViewById(R.id.inputName);
        matrix = findViewById(R.id.inputMatrix);
        email = findViewById(R.id.inputEmail);
        phone = findViewById(R.id.inputPhone);
        password = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
        registerButton = findViewById(R.id.registerButton);
        cancelButton = findViewById(R.id.cancelButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(view -> {
            if(validateFields()){
                String matriculationNumber = matrix.getEditText().getText().toString().trim();
                String userEmail = email.getEditText().getText().toString().trim();
                String userPassword = password.getEditText().getText().toString().trim();

                // Create a new user with email and password
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Registration success, update UI with the signed-in user's information
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    // Now you have the authenticated user, you can proceed to store additional user data
                                    checkMatriculationNumber(matriculationNumber);
                                }
                            } else {
                                // If registration fails, display a message to the user.
                                Toast.makeText(RegisterPage.this, "Registration failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        });

        cancelButton.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void checkMatriculationNumber(String matriculationNumber) {
        DocumentReference userRef = db.collection("users").document(matriculationNumber);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    // Document with this matriculation number already exists
                    matrix.setError("Matriculation number is already registered");
                } else {
                    // Document with this matriculation number doesn't exist
                    matrix.setError(null);

                    // Proceed to store
                    // Retrieve the FCM token
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && task2.getResult() != null) {
                                    String fcmToken = task2.getResult();

                                    // Now you have the FCM token, you can store it or use it as needed
                                    // Store user data in Firestore along with the FCM token
                                    storeUserData(fcmToken);
                                } else {
                                    // Handle the error
                                    Exception exception = task2.getException();
                                    if (exception != null) {
                                        // Handle the exception
                                    }
                                }
                            });
                }
            } else {
                // Handle errors
                Toast.makeText(RegisterPage.this, "Error checking matriculation number: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Register Page", task.getException().getMessage());
            }
        });
    }

    private void storeUserData(String fcmToken) {
        String matriculationNumber = matrix.getEditText().getText().toString();
        String userName = name.getEditText().getText().toString();
        String userEmail = email.getEditText().getText().toString();
        String userPhone = phone.getEditText().getText().toString();

        // Create a new document with the specified matriculation number
        DocumentReference newUserRef = db.collection("users").document(matriculationNumber);

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        userData.put("email", userEmail);
        userData.put("phone", userPhone);
        userData.put("fcmToken", fcmToken);

        // Set the data of the document to the User object
        newUserRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Handle success, e.g., show a success message
                    Toast.makeText(RegisterPage.this, "Registration successful", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("UserPreference", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("matriculationNo", matriculationNumber);
                    editor.putString("userName", userName);
                    editor.putString("email", userEmail);
                    editor.putString("phone", userPhone);
                    editor.putString("status", "login");

                    editor.apply();

                    Intent intent = new Intent(RegisterPage.this, HomePage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle failure, e.g., show an error message
                    Toast.makeText(RegisterPage.this, "Error storing user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private boolean validateFields() {
        boolean isValid = true;

        if (TextUtils.isEmpty(name.getEditText().getText())) {
            name.setError("Name is required");
            isValid = false;
        } else {
            name.setError(null);
        }

        if (TextUtils.isEmpty(matrix.getEditText().getText())) {
            matrix.setError("Matriculation No is required");
            isValid = false;
        } else {
            matrix.setError(null);
        }

        if (TextUtils.isEmpty(email.getEditText().getText())) {
            email.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getEditText().getText()).matches()) {
            email.setError("Invalid email address");
            isValid = false;
        } else {
            email.setError(null);
        }

        if (TextUtils.isEmpty(password.getEditText().getText())) {
            password.setError("Password is required");
            isValid = false;
        } else if (password.getEditText().getText().length() < 6) {
            password.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            password.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword.getEditText().getText())) {
            confirmPassword.setError("Confirm password is required");
            isValid = false;
        } else if (!confirmPassword.getEditText().getText().toString().equals(password.getEditText().getText().toString())) {
            confirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            confirmPassword.setError(null);
        }


        return isValid;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterPage.this,LoginPage.class);
        startActivity(intent);
        finish();
    }
}