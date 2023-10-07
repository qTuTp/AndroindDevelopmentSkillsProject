package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

//TODO: Add tooltips for progress bar to display percentage
public class HomePage extends AppCompatActivity {

    private AppCompatButton manageParkButton;
    private AppCompatButton manageCarButton;
    private TextView carPlateNum;
    private ProgressBar floor1;
    private ProgressBar basement1;
    private ProgressBar basement2;
    private ProgressBar garden;

    //Declare the max vacancy
    private final int FLOOR1_MAX = 100;
    private final int BASEMENT1_MAX = 100;
    private final int BASEMENT2_MAX = 100;
    private final int GARDEN_MAX = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setReference();

        int floor1Occupation = 10;
        int basement1Occupation = 40;
        int basement2Occupation = 50;
        int gardenOccupation = 87;

        //Calculate the percentage of the occupation
        int floor1Percentage = (floor1Occupation*100)/FLOOR1_MAX;
        int basement1Percentage = (basement1Occupation*100)/BASEMENT1_MAX;
        int basement2Percentage = (basement2Occupation*100)/BASEMENT2_MAX;
        int gardenPercentage = (gardenOccupation*100)/GARDEN_MAX;

        updateProgressBar(floor1,floor1Percentage);
        updateProgressBar(basement1,basement1Percentage);
        updateProgressBar(basement2,basement2Percentage);
        updateProgressBar(garden,gardenPercentage);


    }

    protected void setReference(){
        manageParkButton = findViewById(R.id.parkManage);
        manageCarButton = findViewById(R.id.carManage);
        carPlateNum = findViewById(R.id.carPlateNumber);
        floor1 = findViewById(R.id.floor1ProgressBar);
        basement1 = findViewById(R.id.basement1ProgressBar);
        basement2 = findViewById(R.id.basement2ProgressBar);
        garden = findViewById(R.id.gardenProgressBar);

    }

    //Update the corresponding data on the visual of progress bar
    protected void updateProgressBar(ProgressBar p, int percentage){

        if (percentage <= 40) {
            p.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.custom_progress_bar_light));
        } else if (percentage <= 70) {
            p.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.custom_progress_bar_medium));
        } else {
            p.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.custom_progress_bar_light));
        }

        //Setting the value to progress bar
        p.setProgress(percentage);

    }


}