package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;


public class HomePage extends AppCompatActivity {

    private AppCompatButton manageParkButton;
    private AppCompatButton manageCarButton;
    private AppCompatButton parkSelectButton;
    private AppCompatButton searchVehicleButton;
    private AppCompatButton reportButton;
    private AppCompatButton profileButton;
    private AppCompatButton peakTimeButton;
    private AppCompatButton notificationButton;
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

    //Assign Reference to the element in layout file
    protected void setReference(){
        manageParkButton = findViewById(R.id.parkManage);
        manageCarButton = findViewById(R.id.carManage);
        carPlateNum = findViewById(R.id.carPlateNumber);
        floor1 = findViewById(R.id.floor1ProgressBar);
        basement1 = findViewById(R.id.basement1ProgressBar);
        basement2 = findViewById(R.id.basement2ProgressBar);
        garden = findViewById(R.id.gardenProgressBar);
        parkSelectButton = findViewById(R.id.parkSelectButton);
        searchVehicleButton = findViewById(R.id.seachCarButton);
        profileButton = findViewById(R.id.profileButton);
        peakTimeButton = findViewById(R.id.peakTimeButton);
        reportButton = findViewById(R.id.reportButton);
        notificationButton = findViewById(R.id.notificationButton);

        //progress bar will display tooltip on click
        floor1.setOnClickListener(view -> showTooltipForProgress(floor1));
        basement1.setOnClickListener(view -> showTooltipForProgress(basement1));
        basement2.setOnClickListener(view -> showTooltipForProgress(basement2));
        garden.setOnClickListener(view -> showTooltipForProgress(garden));


        manageParkButton.setOnClickListener(v -> {

        });
        manageCarButton.setOnClickListener( v -> {

        });
        parkSelectButton.setOnClickListener( v -> {

        });
    }

    //Update the corresponding data on the visual of progress bar
    private void updateProgressBar(ProgressBar p, int percentage){

        if (percentage <= 40) {
            p.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.custom_progress_bar_light));
        } else if (percentage <= 70) {
            p.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.custom_progress_bar_medium));
        } else {
            p.setProgressDrawable(AppCompatResources.getDrawable(this, R.drawable.custome_progress_bar_heavy));
        }

        //Setting the value to progress bar
        p.setProgress(percentage);

    }

    //It will show the current percentage of the clicked progress bar
    private void showTooltipForProgress(ProgressBar progressBar) {
        int percentage = progressBar.getProgress();
        new SimpleTooltip.Builder(this)
                .anchorView(progressBar)
                .text(percentage + "%")
                .gravity(Gravity.END)
                .animated(true)
                .showArrow(false)
                .transparentOverlay(true)
                .backgroundColor(getColor(R.color.white))
                .textColor(getColor(R.color.INTIRed))
                .build()
                .show();
    }


}