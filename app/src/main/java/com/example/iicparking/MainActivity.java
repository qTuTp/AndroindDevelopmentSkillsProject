package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton loginP;
    private AppCompatButton regisP;
    private AppCompatButton homeP;
    private AppCompatButton peakP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences("UserPreference", Context.MODE_PRIVATE);

        if (pref.getString("status","").equals("login")){
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
            finish();

        } else {
            Intent intent = new Intent(MainActivity.this, LoginPage.class);
            startActivity(intent);
            finish();
        }
        setReference();


    }

    private void setReference(){
        loginP = findViewById(R.id.loginP);
        regisP = findViewById(R.id.registerP);
        homeP = findViewById(R.id.homeP);
        peakP = findViewById(R.id.peakP);

        loginP.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginPage.class);
            startActivity(intent);
        });

        regisP.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterPage.class);
            startActivity(intent);
        });

        homeP.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
        });

        peakP.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PeakTimePage.class);
            startActivity(intent);
        });

    }
}