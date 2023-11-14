package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginPage extends AppCompatActivity {

    private TextInputLayout email;
    private TextInputLayout password;
    private TextView forgetPasswordButton;
    private AppCompatButton logButton;
    private TextView signupButton;
    private FirebaseFirestore db;
    private ProgressBar loadingIndicator;
    private Dialog forgetPasswordDialog;
    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        setReference();


    }

    //Set the reference and listener to the element in the layout file
    protected void setReference(){
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        forgetPasswordButton = findViewById(R.id.forgetPasswordButton);
        logButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        db = FirebaseFirestore.getInstance();
        loadingIndicator = findViewById(R.id.loadingIndicator);
        auth = FirebaseAuth.getInstance();


        forgetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgetPasswordDialog();

            }
        });

        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Retrieve user input
                String enteredEmail = email.getEditText().getText().toString().trim();
                String enteredPassword = password.getEditText().getText().toString().trim();

                if (enteredEmail.isEmpty()){
                    email.setError("Email is required!");
                } else if (enteredPassword.isEmpty()) {
                    password.setError("Password is required!");

                }  else {
                    email.setError(null);
                    password.setError(null);
                    // Perform login
                    loginUser(enteredEmail, enteredPassword);
                }


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

    private void loginUser(String emailStr, String password) {
        email.setError(null);
        this.password.setError(null);
        loadingIndicator.setVisibility(View.VISIBLE);


        // Sign in with email and password using Firebase Authentication
        auth.signInWithEmailAndPassword(emailStr, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Now you have the authenticated user, you can proceed to store additional user data
                            getUserData(emailStr);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        email.setError("Email or Password is Incorrect");
                        this.password.setError("Email or Password is Incorrect");
                        loadingIndicator.setVisibility(View.INVISIBLE);
                    }
                });
        // Query Firestore to find a user with the entered matrix number and password

    }

    private void getUserData(String userEmail) {
        // Query Firestore to get additional user data
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if a user with the entered email exists in Firestore
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot userSnapshot = task.getResult().getDocuments().get(0);
                            String matricNumber = userSnapshot.getId();
                            String userName = userSnapshot.getString("name");
                            String userPhone = userSnapshot.getString("phone");

                            // Store user information in SharedPreferences
                            storeUserData(matricNumber, userName, userEmail, userPhone);

                            // Retrieve the FCM token and update it in Firestore
                            updateFCMToken(matricNumber);

                        } else {
                            // User not found, display an error message
                            email.setError("Email or Password is Incorrect");
                            this.password.setError("Email or Password is Incorrect");
                            loadingIndicator.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        // Handle errors
                        String errorMessage = "Error checking credentials: " + task.getException().getMessage();
                        Toast.makeText(LoginPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("LoginPage", errorMessage);
                        loadingIndicator.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void storeUserData(String matricNumber, String userName, String userEmail, String userPhone) {
        // Store user information in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("matriculationNo", matricNumber);
        editor.putString("userName", userName);
        editor.putString("email", userEmail);
        editor.putString("phone", userPhone);
        editor.putString("status", "login");
        editor.apply();
    }

    private void updateFCMTokenWithRetry(String matricNumber, int remainingRetries) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();
                        DocumentReference userRef = db.collection("users").document(matricNumber);
                        userRef.update("fcmToken", fcmToken)
                                .addOnSuccessListener(aVoid -> {
                                    loadingIndicator.setVisibility(View.INVISIBLE);
                                    // Update successful
                                    Intent intent = new Intent(LoginPage.this, HomePage.class);
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
                                        updateFCMTokenWithRetry(matricNumber, newRemainingRetries);
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
    private void updateFCMToken(String matricNumber) {
        int maxRetries = 3;
        updateFCMTokenWithRetry(matricNumber, maxRetries);
    }

    private void showForgetPasswordDialog() {
        forgetPasswordDialog = new Dialog(this);
        forgetPasswordDialog.setContentView(R.layout.forget_password_popup_email);
        forgetPasswordDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        forgetPasswordDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        forgetPasswordDialog.setCancelable(true);

        TextInputLayout email = forgetPasswordDialog.findViewById(R.id.emailInput);
        MaterialButton buttonSubmitMatricNo = forgetPasswordDialog.findViewById(R.id.continueButton);
        TextView buttonCancelMatricNo = forgetPasswordDialog.findViewById(R.id.forgetPasswordReturnButton);
//        ProgressBar loadingIndicator = forgetPasswordDialog.findViewById(R.id.forgetPasswordLoadingIndicator);

        // Set onClickListener for the submit button
        buttonSubmitMatricNo.setOnClickListener(v -> {
            loadingIndicator.setVisibility(View.VISIBLE);
            String emailText = email.getEditText().getText().toString().trim();

            // Perform validation on enteredMatricNo (you may want to check for empty input, etc.)
            if (!emailText.isEmpty()) {
                sendPasswordResetEmail(emailText);
            } else {
                email.setError("Email is required");
                loadingIndicator.setVisibility(View.INVISIBLE);
            }

            forgetPasswordDialog.dismiss();

        });

        // Set onClickListener for the cancel button
        buttonCancelMatricNo.setOnClickListener(v -> forgetPasswordDialog.dismiss());

        // Show the Matric No input dialog
        forgetPasswordDialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Password reset email sent successfully
                        Toast.makeText(LoginPage.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle password reset email failure
                        Toast.makeText(LoginPage.this, "Error sending password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("LoginPage", "Error sending password reset email", task.getException());
                    }
                    loadingIndicator.setVisibility(View.INVISIBLE);
                });
    }


//    private void showPasswordResetDialog(String matricNo) {
//        forgetPasswordDialog.dismiss();
//        // Create the password reset dialog
//        Dialog passwordResetDialog = new Dialog(this);
//        passwordResetDialog.setContentView(R.layout.forget_password_popup_password);
//        passwordResetDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//        passwordResetDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
//        passwordResetDialog.setCancelable(true);
//
//        TextInputLayout newPasswordText = passwordResetDialog.findViewById(R.id.newPassword);
//        TextInputLayout confirmPasswordText = passwordResetDialog.findViewById(R.id.confirmPassword);
//        MaterialButton buttonSubmitPasswordReset = passwordResetDialog.findViewById(R.id.confirmPassword);
//        MaterialButton buttonCancelPasswordReset = passwordResetDialog.findViewById(R.id.cancelButton);
//
//        // Set onClickListener for the submit button
//        buttonSubmitPasswordReset.setOnClickListener(v -> {
//            String newPassword = newPasswordText.getEditText().getText().toString().trim();
//            String confirmPassword = confirmPasswordText.getEditText().getText().toString().trim();
//
//            if (newPassword.length()<8) {
//                newPasswordText.setError("Password must be minimum 8 character");
//
//            } else if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$")) {
//                newPasswordText.setError("Password must contain a alphabet character and numerical character.");
//            } else if (!newPassword.equals(confirmPassword)) {
//                confirmPasswordText.setError("Password must be the same.");
//            }else {
//                newPasswordText.setError(null);
//                confirmPasswordText.setError(null);
//
//                checkOldPasswordAndSave(matricNo, newPassword);
//            }
//        });
//
//
//        // Set onClickListener for the cancel button
//        buttonCancelPasswordReset.setOnClickListener(v -> passwordResetDialog.dismiss());
//
//        // Show the password reset dialog
//        passwordResetDialog.show();
//    }

//    private void checkOldPasswordAndSave(String matricNo, String newPassword) {
//        // Query Firestore to get the old password
//        db.collection("users")
//                .document(matricNo)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot userSnapshot = task.getResult();
//                        if (userSnapshot != null) {
//                            String oldPassword = userSnapshot.getString("password");
//
//                            // Check if the new password is the same as the old password
//                            if (newPassword.equals(oldPassword)) {
//                                // Display an error message for same old and new passwords
//                                Toast.makeText(LoginPage.this, "New password must be different from the old password", Toast.LENGTH_SHORT).show();
//
//                            } else {
//                                // Update the new password in Firestore
//                                updatePasswordInFirestore(matricNo, newPassword);
//                            }
//                        }
//                    } else {
//                        // Handle errors
//                        String errorMessage = "Error checking old password: " + task.getException().getMessage();
//                        Toast.makeText(LoginPage.this, errorMessage, Toast.LENGTH_SHORT).show();
//                        Log.e("LoginPage", errorMessage);
//                    }
//                });
//    }

//    private void updatePasswordInFirestore(String matricNo, String newPassword) {
//        // Update the new password in Firestore
//        db.collection("users")
//                .document(matricNo)
//                .update("password", newPassword)
//                .addOnSuccessListener(aVoid -> {
//                    // Password update successful
//                    Toast.makeText(LoginPage.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    // Handle update failure
//                    String errorMessage = "Error updating password: " + e.getMessage();
//                    Toast.makeText(LoginPage.this, errorMessage, Toast.LENGTH_SHORT).show();
//                    Log.e("LoginPage", errorMessage);
//                });
//    }



}


