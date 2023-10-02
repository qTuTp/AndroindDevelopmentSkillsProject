package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class RegisterPage extends AppCompatActivity {


    //Declare Variable
    private EditText name;
    private EditText id;
    private EditText email;
    private EditText phone;
    private EditText password;
    private EditText confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        //Assigning edittext variable
        name = findViewById(R.id.inputName);
        id = findViewById(R.id.inputId);
        email = findViewById(R.id.inputEmail);
        phone = findViewById(R.id.inputPhone);
        password = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
    }
}