package com.example.iicparking;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "parkLog";

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
        List<BarEntry> barEntryList = new ArrayList<>();
        updateChart(barEntryList);
        db = FirebaseFirestore.getInstance();
        fetchDataFromFirestore();


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

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    //To get the current date
    private void getToday(){
        Calendar calendar = Calendar.getInstance();
        currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;

    }

    private void fetchDataFromFirestore() {
        // Get the date of the selected day
        Calendar calendar = Calendar.getInstance();
        String selectedDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        Log.d("SelectedDate", selectedDate);

        // Fetch data from Firestore
        db.collection("parkLog")
                .document(selectedDate)
                .collection("log")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<Integer, Integer> hourCarCountMap = new HashMap<>();

                    // Iterate through documents in the "log" subcollection
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Get start and end times from the document
                        long startTime = document.getLong("startTime");
                        long endTime = document.getLong("endTime");

                        // Calculate total car count for each hour within the range
                        for (long hour = startTime; hour <= endTime; hour++) {
                            // Adjust the hour to be between 0 and 23
                            int adjustedHour = (int) (hour % 24);

                            // Increment car count for the hour in the map
                            hourCarCountMap.put(adjustedHour, hourCarCountMap.getOrDefault(adjustedHour, 0) + 1);
                        }
                        Log.d("FirestoreData", "Data: " + queryDocumentSnapshots.toString());
                    }

                    // Convert the map to a list of BarEntry objects
                    List<BarEntry> values = new ArrayList<>();
                    for (Map.Entry<Integer, Integer> entry : hourCarCountMap.entrySet()) {
                        values.add(new BarEntry(entry.getKey(), entry.getValue()));
                    }

                    // Update the chart with the calculated values
                    updateChart(values);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("FirestoreError", "Error fetching data", e);
                });
    }


        //Update the visual of the chart
    private void updateChart(List<BarEntry> values) {
        // Assign color for the bar based on value
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : values) {
            colors.add(getColorForEntry(entry));
        }
        Log.d("ChartUpdate", "Updating chart with values: " + values.toString());
        BarDataSet set = new BarDataSet(values, "Number of Cars");
        set.setColors(colors);

        BarData barData = new BarData(set);
        peakTimeChart.setData(barData);

        peakTimeChart.setHighlightFullBarEnabled(false);
        peakTimeChart.setHighlightPerTapEnabled(false);
        peakTimeChart.setHighlightPerDragEnabled(false);

        // Set up the y-axis
        YAxis yAxis = peakTimeChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawLabels(true);
        yAxis.setGranularity(1f);
        yAxis.setGranularityEnabled(true);

        // Set up the x-axis
        XAxis xAxis = peakTimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        // Set up the labels for the y-axis
        final String[] hours = new String[]{"0000", "0100", "0200", "0300", "0400", "0500", "0600", "0700", "0800", "0900", "1000", "1100",
                "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100", "2200", "2300"};

        yAxis.setValueFormatter(new IndexAxisValueFormatter(hours));

        // Set up the labels for the x-axis
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Ensure the value is a valid index in the 'values' list
                int index = (int) value;
                if (index >= 0 && index < values.size()) {
                    return String.valueOf((int) values.get(index).getY());
                } else {
                    return "";
                }
            }
        });

        YAxis rightAxis = peakTimeChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable the right axis

        peakTimeChart.getLegend().setEnabled(false);
        peakTimeChart.getDescription().setEnabled(false);

        peakTimeChart.invalidate();
    }


    private int getColorForEntry(BarEntry entry) {

        int highColor = ContextCompat.getColor(this, R.color.progressHeavy);
        int mediumColor = ContextCompat.getColor(this, R.color.progressMedium);
        int lowColor = ContextCompat.getColor(this, R.color.progressLight);

        if (entry.getY() > 100) {
            return highColor;
        } else if (entry.getY() > 50) {
            return mediumColor;
        } else {
            return lowColor;
        }
    }

}


