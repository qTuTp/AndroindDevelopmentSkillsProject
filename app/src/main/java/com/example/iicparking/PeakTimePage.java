package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PeakTimePage extends AppCompatActivity {

    private class HourAxisValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            int hour = 23 - (int) value;
            return String.format(Locale.ENGLISH, "%02d:00", hour);
        }

    }

    private TextView primaryDay;
    private TextView priorDay;
    private TextView afterDay;
    private HorizontalBarChart peakTimeChart;

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
        updateChart();

    }

    private void setReference(){
        primaryDay = findViewById(R.id.primaryDay);
        priorDay = findViewById(R.id.priorDay);
        afterDay = findViewById(R.id.afterDay);
        DAYS = getResources().getStringArray(R.array.days_of_week);
        peakTimeChart = findViewById(R.id.peakTimeChart);


        //indicator 1 mean prior day is clicked, while 2 indicate after day is clicked
        priorDay.setOnClickListener(v -> updateDay(1));
        afterDay.setOnClickListener(v -> updateDay(2));
    }

    private void updateDay(int indicator){

        switch (indicator){
            case 0: //Initialize layout
                //Set the visibility to invisible
                priorDay.setVisibility(View.INVISIBLE);
                primaryDay.setVisibility(View.INVISIBLE);
                afterDay.setVisibility(View.INVISIBLE);
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
                //Set the visibility to invisible
                priorDay.setVisibility(View.INVISIBLE);
                primaryDay.setVisibility(View.INVISIBLE);
                afterDay.setVisibility(View.INVISIBLE);

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
                //Set the visibility to visible
                priorDay.setVisibility(View.VISIBLE);
                primaryDay.setVisibility(View.VISIBLE);
                afterDay.setVisibility(View.VISIBLE);

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

    //Update the visual of the chart
    private void updateChart(){
        List<BarEntry> values = new ArrayList<>();


        // Suppose you're collecting data every hour over 24 hours.
        // The reason of start from 23 and decrementing is because otherwise the graph will show top as 2300 and bottom as 0000
        // For now, view the hour as the actual time, eg. 23 is 2300h. Ignore the 23 - hour as when i think my kepala also sakit, i use this just for
        // Easier visualize of time only, if i use start from 0 then we may get confuse because the 0000h time maybe contain the data of 2300h
        for (int hour = 23; hour >= 0; hour--) {
            float randomCarsCount = (float) (Math.random() * 500); // Random number of cars
            values.add(new BarEntry(23 - hour, randomCarsCount));
        }



        //Assign color for the bar based on value
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : values) {
            colors.add(getColorForEntry(entry));
        }

        BarDataSet set = new BarDataSet(values, "Number of Cars");
        set.setColors(colors);

        BarData barData = new BarData(set);
        peakTimeChart.setData(barData);

        peakTimeChart.setHighlightFullBarEnabled(false);
        peakTimeChart.setHighlightPerTapEnabled(false);
        peakTimeChart.setHighlightPerDragEnabled(false);


        XAxis xAxis = peakTimeChart.getXAxis();
        xAxis.setValueFormatter(new HourAxisValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = peakTimeChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);

        YAxis rightAxis = peakTimeChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);

        peakTimeChart.getLegend().setEnabled(false);

        peakTimeChart.getDescription().setEnabled(false);

        peakTimeChart.invalidate();


    }

    private int getColorForEntry(BarEntry entry) {

        int highColor = ContextCompat.getColor(this, R.color.progressHeavy);
        int mediumColor = ContextCompat.getColor(this, R.color.progressMedium);
        int lowColor = ContextCompat.getColor(this, R.color.progressLight);

        if (entry.getY() > 400) {
            return highColor;
        } else if (entry.getY() > 250) {
            return mediumColor;
        } else {
            return lowColor;
        }
    }

}


