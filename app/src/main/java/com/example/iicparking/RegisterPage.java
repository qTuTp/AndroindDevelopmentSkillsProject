package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterPage extends AppCompatActivity {


    //Declare Variable
    private TextInputLayout name;
    private TextInputLayout matrix;
    private TextInputLayout email;
    private TextInputLayout phone;
    private TextInputLayout password;
    private TextInputLayout confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        //Assigning edittext variable
        name = findViewById(R.id.inputName);
        matrix = findViewById(R.id.inputMatrix);
        email = findViewById(R.id.inputEmail);
        phone = findViewById(R.id.inputPhone);
        password = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
    }
}