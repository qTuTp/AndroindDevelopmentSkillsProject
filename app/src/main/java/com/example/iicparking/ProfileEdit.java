package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileEdit extends AppCompatActivity {
    private final String TAG = "ProfileEditPage";

    private TextInputLayout name;
    private TextInputLayout phone;
    private MaterialButton saveButton;
    private MaterialButton discardButton;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        setReference();


    }

    private void updateData(){
        SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        String nameStr = prefs.getString("userName", "");
        String phoneStr = prefs.getString("phone", "");

        name.getEditText().setText(nameStr);
        phone.getEditText().setText(phoneStr);
    }

    private void setReference(){
        db = FirebaseFirestore.getInstance();

        name = findViewById(R.id.nameInput);
        phone = findViewById(R.id.phoneInput);
        saveButton = findViewById(R.id.saveButton);
        discardButton = findViewById(R.id.discardButton);

        updateData();

        saveButton.setOnClickListener(view -> {
            validateData();
        });

        discardButton.setOnClickListener(view -> {
            finish();
        });

    }

    private void validateData(){
        name.setError(null);
        phone.setError(null);

        SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        String matric = prefs.getString("matriculationNo", "");
        String nameStr = prefs.getString("userName", "");
        String phoneStr = prefs.getString("phone", "");

        String editNameStr = name.getEditText().getText().toString().trim();
        String editPhoneStr = phone.getEditText().getText().toString().trim();

        if(nameStr.equals(editNameStr) && phoneStr.equals(editPhoneStr)){
            Toast.makeText(this, "All Info Remain Unchange!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (editNameStr.isEmpty()){
            name.setError("Name is Required !");
            return;
        }

        if (editPhoneStr.isEmpty()){
            phone.setError("Phone is Required !");
            return;
        }

        saveDataToFirestore(matric, editNameStr, editPhoneStr);


    }

    private void saveDataToFirestore(String matric, String newName, String newPhone) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assume you have a collection named "users"
        CollectionReference usersRef = db.collection("users");

        // Assume each user document is identified by their matriculation number
        DocumentReference userDocRef = usersRef.document(matric);

        // Create a map to update the user's data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newName);
        userData.put("phone", newPhone);

        // Update the user document in Firestore
        userDocRef.update(userData)
                .addOnSuccessListener(d -> {
                    // Update SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userName", newName);
                    editor.putString("phone", newPhone);
                    editor.apply();

                    finish();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating user data", e));
    }
}