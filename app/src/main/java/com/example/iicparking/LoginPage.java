package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginPage extends AppCompatActivity {

    private TextInputLayout matrix;
    private TextInputLayout password;
    private TextView forgetPasswordButton;
    private AppCompatButton logButton;
    private TextView signupButton;
    private FirebaseFirestore firestore;
    private ProgressBar loadingIndicator;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        setReference();


    }

    //Set the reference and listener to the element in the layout file
    protected void setReference(){
        matrix = findViewById(R.id.matrixInput);
        password = findViewById(R.id.passwordInput);
        forgetPasswordButton = findViewById(R.id.forgetPasswordButton);
        logButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        firestore = FirebaseFirestore.getInstance();
        loadingIndicator = findViewById(R.id.loadingIndicator);

        forgetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve user input
                String enteredMatrix = matrix.getEditText().getText().toString().trim();
                String enteredPassword = password.getEditText().getText().toString().trim();

                // Perform login
                loginUser(enteredMatrix, enteredPassword);

            }
        });

        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //Direct the register page when clicked
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPage.this, RegisterPage.class);
                startActivity(intent);
                finish();
            }
        });




    }

    private void loginUser(String matrixNumber, String password) {
        matrix.setError(null);
        this.password.setError(null);
        loadingIndicator.setVisibility(View.VISIBLE);

        // Query Firestore to find a user with the entered matrix number and password
        firestore.collection("users")
                .whereEqualTo("matrixNumber", matrixNumber)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if a user with the entered credentials exists
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot userSnapshot = task.getResult().getDocuments().get(0);

                            String userName = userSnapshot.getString("userName");
                            String userEmail = userSnapshot.getString("email");
                            String userPhone = userSnapshot.getString("phone");
                            String userPassword = userSnapshot.getString("password");

                            // Store user information in SharedPreferences
                            storeUserData(matrixNumber, userName, userEmail, userPhone, userPassword);

                            // Retrieve the FCM token and update it in Firestore
                            updateFCMToken(matrixNumber);

                        } else {
                            // User not found, display an error message
                            matrix.setError("Matriculation No or Password is Incorrect");
                            this.password.setError("Matriculation No or Password is Incorrect");
                        }
                    } else {
                        // Handle errors
                        String errorMessage = "Error checking credentials: " + task.getException().getMessage();
                        Toast.makeText(LoginPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("LoginPage", errorMessage);
                    }
                });
    }

    private void storeUserData(String matrixNumber, String userName, String userEmail, String userPhone, String userPassword) {
        // Store user information in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("matriculationNo", matrixNumber);
        editor.putString("userName", userName);
        editor.putString("email", userEmail);
        editor.putString("phone", userPhone);
        editor.putString("status", "login");
        editor.apply();
    }

    private void updateFCMTokenWithRetry(String matrixNumber, int remainingRetries) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();
                        DocumentReference userRef = firestore.collection("users").document(matrixNumber);
                        userRef.update("fcmToken", fcmToken)
                                .addOnSuccessListener(aVoid -> {
                                    loadingIndicator.setVisibility(View.INVISIBLE);
                                    // Update successful
                                    Intent intent = new Intent(LoginPage.this, HomePage.class);
                                    intent.putExtra("matrixNumber", matrixNumber);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle update failure
                                    String errorMessage = "Error updating FCM token: " + e.getMessage();
                                    Toast.makeText(LoginPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    Log.e("LoginPage", errorMessage);

                                    // Retry the update with remaining retries
                                    if (remainingRetries > 0) {
                                        int newRemainingRetries = remainingRetries - 1;
                                        updateFCMTokenWithRetry(matrixNumber, newRemainingRetries);
                                    } else {
                                        // No more retries, show a message or take further action as needed
                                        loadingIndicator.setVisibility(View.INVISIBLE);
                                        Toast.makeText(LoginPage.this, "Exceeded update retries", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Handle FCM token retrieval failure
                        Toast.makeText(LoginPage.this, "Error retrieving FCM token", Toast.LENGTH_SHORT).show();
                        Log.e("LoginPage", "Error retrieving FCM token");
                    }
                });
    }

    // Call this method initially with the maximum number of retries
    private void updateFCMToken(String matrixNumber) {
        int maxRetries = 3;
        updateFCMTokenWithRetry(matrixNumber, maxRetries);
    }
}


