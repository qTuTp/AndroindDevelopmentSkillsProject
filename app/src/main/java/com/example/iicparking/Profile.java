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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class Profile extends AppCompatActivity {

    private TextView name;
    private TextView matriculationNo;
    private TextView email;
    private TextView phone;
    private AppCompatButton backButton;
    private MaterialButton editProfileButton;
    private MaterialButton changePasswordButton;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setReference();

        SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        String nameStr = prefs.getString("userName", "");
        String matricNoStr = prefs.getString("matriculationNo", "");
        String phoneStr = prefs.getString("phone", "");
        String emailStr = prefs.getString("email","");

        name.setText(nameStr);
        matriculationNo.setText(matricNoStr);
        phone.setText(phoneStr);
        email.setText(emailStr);

    }

    private void setReference(){
        auth = FirebaseAuth.getInstance();

        name = findViewById(R.id.nameText);
        matriculationNo = findViewById(R.id.matricText);
        email = findViewById(R.id.emailText);
        phone = findViewById(R.id.phoneText);
        editProfileButton = findViewById(R.id.editProfileButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(view -> finish());

        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, ProfileEdit.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(view -> {
            showChangePasswordDialog();
        });

    }

    private void showChangePasswordDialog() {
        SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        String emailStr = prefs.getString("email", "");
        Dialog changePasswordDialog = new Dialog(this);
        changePasswordDialog.setContentView(R.layout.change_password_email_dialog);
        changePasswordDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        changePasswordDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        changePasswordDialog.setCancelable(true);

        TextInputLayout email = changePasswordDialog.findViewById(R.id.emailInput);
        MaterialButton buttonSubmitMatricNo = changePasswordDialog.findViewById(R.id.continueButton);
        TextView buttonCancelMatricNo = changePasswordDialog.findViewById(R.id.returnButton);

        email.getEditText().setText(emailStr);

        // Set onClickListener for the submit button
        buttonSubmitMatricNo.setOnClickListener(v -> {

            sendPasswordResetEmail(emailStr);

            changePasswordDialog.dismiss();

        });

        // Set onClickListener for the cancel button
        buttonCancelMatricNo.setOnClickListener(v -> changePasswordDialog.dismiss());

        // Show the Matric No input dialog
        changePasswordDialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Password reset email sent successfully
                        Toast.makeText(Profile.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle password reset email failure
                        Toast.makeText(Profile.this, "Error sending password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("LoginPage", "Error sending password reset email", task.getException());
                    }
                });
    }
}