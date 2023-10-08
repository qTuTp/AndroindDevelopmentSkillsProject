package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PeakTimePage extends AppCompatActivity {

    private TextView primaryDay;
    private TextView priorDay;
    private TextView afterDay;

    private int currentDay;
    private String[] DAYS;

    private enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,THURSDAY, FRIDAY, SATURDAY;

        public Day toPreviousDay(){
            return values()[(ordinal() - 1 + 7) % 7 ];
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
        updateDay(0);

    }

    private void setReference(){
        primaryDay = findViewById(R.id.primaryDay);
        priorDay = findViewById(R.id.priorDay);
        afterDay = findViewById(R.id.afterDay);
        DAYS = getResources().getStringArray(R.array.days_of_week);


        //indicator 1 mean prior day is clicked, while 2 indicate after day is clicked
        priorDay.setOnClickListener(v -> updateDay(1));
        afterDay.setOnClickListener(v -> updateDay(2));
    }

    private void updateDay(int indicator){

        switch (indicator){
            case 0: //Initialize layout
                break;
            case 1: //If prior day, move the primary day to the previous day
                currentDay = (currentDay - 1 + 7) % 7;
                break;
            case 2: //If after day, move the primary day to the day after
                currentDay = (currentDay + 1 + 7) % 7;
                break;

        }

        Day today = Day.values()[currentDay];
        animateTextUpdate(today);
    }

    private void animateTextUpdate(Day today){
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);



        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                //The text won't be able to be click
                priorDay.setClickable(false);
                primaryDay.setClickable(false);
                afterDay.setClickable(false);
                priorDay.setFocusable(false);
                primaryDay.setFocusable(false);
                afterDay.setFocusable(false);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Update Text
                priorDay.setText(DAYS[today.toPreviousDay().ordinal()]);
                primaryDay.setText(DAYS[today.ordinal()]);
                afterDay.setText(DAYS[today.toNextDay().ordinal()]);

                //Fade in
                priorDay.startAnimation(fadeIn);
                primaryDay.startAnimation(fadeIn);
                afterDay.startAnimation(fadeIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Restore the clickable and focusable
                priorDay.setClickable(true);
                primaryDay.setClickable(true);
                afterDay.setClickable(true);
                priorDay.setFocusable(true);
                primaryDay.setFocusable(true);
                afterDay.setFocusable(true);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //Fade out
        priorDay.startAnimation(fadeOut);
        primaryDay.startAnimation(fadeOut);
        afterDay.startAnimation(fadeOut);

    }

    //To get the current date
    private void getToday(){
        Calendar calendar = Calendar.getInstance();
        currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;


    }
}