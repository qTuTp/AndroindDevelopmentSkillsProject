package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PeakTimePage extends AppCompatActivity {

    private TextView primaryDay;
    private TextView priorDay;
    private TextView afterDay;
    private final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private Day currentDay;
    private Day previousDay;
    private Day laterDay;
    private enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,THURSDAY, FRIDAY, SATURDAY;

        public Day toPreviousDay(){
            return values()[(ordinal() - 1) % 7 ];
        }

        public Day toNextDay(){
            return values()[(ordinal() + 1) % 7 ];
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peak_time_page);
        setReference();
        getToday();

    }

    private void setReference(){
        primaryDay = findViewById(R.id.primaryDay);
        priorDay = findViewById(R.id.priorDay);
        afterDay = findViewById(R.id.afterDay);

        //indicator 1 mean prior day is clicked, while 2 indicate after day is clicked
        priorDay.setOnClickListener(v -> updateDay(1));
        afterDay.setOnClickListener(v -> updateDay(2));
    }

    private void updateDay(int indicator){

        switch (indicator){
            case 0: //Initialize layout
                break;
            case 1: //If prior day, move the primary day to the previous day

                break;
            case 2: //If after day, move the primary day to the day after
                break;
        }
    }

    //To get the current date
    private void getToday(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        currentDay = Day.values()[day - 1];
        previousDay = currentDay.toPreviousDay();
        laterDay = currentDay.toPreviousDay();
    }
}