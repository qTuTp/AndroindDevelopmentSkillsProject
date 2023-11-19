package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Slot_Selection extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private RequestQueue requestQueue;

    private static final String TAG = "SLOTSELECTION";
    Button timeButton, endTime_button;
    int hour, minute;
    private Spinner locationSpinner;
    private MaterialButtonToggleGroup parkTypeToggleGroup;
    private MaterialButton normalParkingButton;
    private MaterialButton doubleParkingButton;
    private MaterialButton submitButton;
    private MaterialButton cancelButton;
    private ConstraintLayout blockedCarViewGroup;
    private LinearLayout blockedCarInputGroup;
    private TextInputLayout startTime;
    private TextInputLayout endTime;
    private TextInputLayout blockedCarInput1;
    private TextView addNewBlockedCar;
    private String currentParkLocation;
    private String parkType;
    private int blockedCarPlateCounter = 1;
    private List<TextInputLayout> blockedCarList;
    private FirebaseFirestore db;
    private String currentVehiclePlate;
    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "key=AAAA74mibkg:APA91bEyq5wGVPe4N6mMrVrKJ_yakqcNNlzand4EiHNL4I5RPXm_aTWLiklOcgzwBVmKnYo_NFbQ5rg8iH5oxB8JlvKAMCYAAwcJt_HyNXzX4vciIlVPQ1bmkqLdxsG70Shgw8b3kcL-";
    private static final String CONTENT_TYPE = "application/json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_selection);

        currentVehiclePlate = getIntent().getStringExtra("currentVehicle");

        setReference();

    }

    private void setReference (){
        requestQueue = Volley.newRequestQueue(this);
        db = FirebaseFirestore.getInstance();

        locationSpinner = findViewById(R.id.spinner_1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.location, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setOnItemSelectedListener(this);

        blockedCarList = new ArrayList<>();

        parkTypeToggleGroup = findViewById(R.id.toggleButton);
        normalParkingButton = findViewById(R.id.normalParkingButton);
        doubleParkingButton = findViewById(R.id.doubleParkingButton);
        blockedCarViewGroup = findViewById(R.id.blockedCarViewGroup);
        blockedCarInputGroup = findViewById(R.id.blockCarInputGroup);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        addNewBlockedCar = findViewById(R.id.addNewBlockedCar);
        blockedCarInput1 = findViewById(R.id.blockedCarInput1);
        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);

        blockedCarList.add(blockedCarInput1);

        endTime.getEditText().setOnClickListener(view -> {
            popTimePicker(endTime);
        });


        addNewBlockedCar.setOnClickListener(view -> {
            addNewBlockedCarInput();
        });

        submitButton.setOnClickListener(view -> {
            validateAndSave();
        });

        cancelButton.setOnClickListener(view -> {
            // Close this activity
            finish();
        });


        // Add a listener to handle button checked changes
        parkTypeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.doubleParkingButton) {
                    // Double parking selected, show the "Block Car" views
                    blockedCarViewGroup.setVisibility(View.VISIBLE);
                    parkType = "Double Parking";
                } else {
                    // Normal parking selected or no selection, hide the "Block Car" views
                    blockedCarViewGroup.setVisibility(View.GONE);
                    parkType = "Normal Parking";
                }
            }
        });

        // Set the default selected button
        normalParkingButton.setChecked(true);
    }

    private void validateAndSave(){
        SharedPreferences pref = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        String matricNo = pref.getString("matriculationNo","");
        String phone = pref.getString("phone","");


        this.endTime.setError(null);
        this.blockedCarInput1.setError(null);

        String currentDate = getCurrentDate();
        String currentTime = getCurrentTime();
        String endTime = this.endTime.getEditText().getText().toString().trim();

        // Check if the end time is earlier than the current time
        if (compareTimes(currentTime, endTime) > 0) {
            this.endTime.setError("End Time cannot be earlier than Current Time");
            return; // Stop further processing
        }

        if (endTime.isEmpty() || endTime.equals("--:--")){
            this.endTime.setError("Please select a End Time");
            return;
        }else {
            // Get all the data
            Map<String, Object> parkLog = new HashMap<>();

            List<String> blockedCarPlate = new ArrayList<>();
            if (parkType.equals("Double Parking")) {
                for (TextInputLayout inputField: blockedCarList){
                    String plate = inputField.getEditText().getText().toString().trim();
                    if (!plate.isEmpty()){
                        blockedCarPlate.add(plate);
                    }
                }

                if (blockedCarPlate.isEmpty()) {
                    blockedCarInput1.setError("At least 1 Car Plate is Required");
                    return;
                }

                //TODO: Add notification to user

                for (String plate: blockedCarPlate){
                    searchUserWithCarPlate(plate, matricNo, phone, currentVehiclePlate);


                }

                parkLog.put("carPlate", currentVehiclePlate);
                parkLog.put("matriculationNo", matricNo);
                parkLog.put("parkType", parkType);
                parkLog.put("location", locationSpinner.getSelectedItem());
                parkLog.put("startTime", currentTime);
                parkLog.put("endTime", endTime);
                parkLog.put("blockedCarPlate", blockedCarPlate);
            }else {
                parkLog.put("matriculationNo", matricNo);
                parkLog.put("parkType", parkType);
                parkLog.put("location", locationSpinner.getSelectedItem());
                parkLog.put("startTime", currentTime);
                parkLog.put("endTime", endTime);
                parkLog.put("carPlate", currentVehiclePlate);
            }

            // Save the parkLog object into Firestore
            CollectionReference parkLogCollection = db.collection("parkLog");
            DocumentReference currentDateDoc = parkLogCollection.document(currentDate);

            // Add a subcollection for each parkLog within the same day
            CollectionReference dayLogCollection = currentDateDoc.collection("logs");
            dayLogCollection.add(parkLog)
                    .addOnSuccessListener(documentReference  -> {
                        // Successfully saved the parkLog
                        // Add any additional actions or UI updates here
                        Toast.makeText(this, "Park Successfully", Toast.LENGTH_SHORT);

                        // Save status of the user park
                        SharedPreferences preferences = getSharedPreferences("ParkPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("documentID", documentReference.getId());
                        editor.putString("status", "parked");
                        editor.putString("location", (String) locationSpinner.getSelectedItem());
                        editor.putString("endTime", endTime);
                        editor.putString("startTime", currentTime);
                        editor.putString("date", currentDate);
                        editor.putString("parkType", parkType);
                        editor.putString("carPlate", currentVehiclePlate);
                        Log.d(TAG, documentReference.getId());

                        editor.apply();



                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to save the parkLog
                        // Handle the error, show a toast, or log the error
                        Toast.makeText(this, "Park Failed", Toast.LENGTH_SHORT);
                    });
        }
    }

    private void sendPushNotification(String userFCMToken, String title, String message) {
        String FCM_API = "https://fcm.googleapis.com/fcm/send";
        String serverKey = "key=AAAA74mibkg:APA91bEyq5wGVPe4N6mMrVrKJ_yakqcNNlzand4EiHNL4I5RPXm_aTWLiklOcgzwBVmKnYo_NFbQ5rg8iH5oxB8JlvKAMCYAAwcJt_HyNXzX4vciIlVPQ1bmkqLdxsG70Shgw8b3kcL-";  // Replace with your actual server key
        String contentType = "application/json";

        JSONObject notificationBody = createNotificationPayload(userFCMToken, title, message);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, FCM_API, notificationBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("TAG", "onResponse: " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Slot_Selection.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i("TAG", "onErrorResponse: " + error.getMessage(), error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private JSONObject createNotificationPayload(String userFCMToken, String title, String message) {
        JSONObject notification = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("title", title);
            data.put("body", message);

            Log.d("TAG", userFCMToken);

            notification.put("to", userFCMToken);
            notification.put("notification", data);

            return notification;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void searchUserWithCarPlate(String carPlate, String matricNo, String phone, String blockCarPlate) {
        Query query = db.collectionGroup("cars").whereEqualTo("carPlateNumber", carPlate);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {



                //Add Notificatiion to user
                for (QueryDocumentSnapshot document : task.getResult()) {
                    DocumentReference userRef = document.getReference().getParent().getParent();


                    //Send push notification to user
                    // Retrieve FCM token from the user document
                    userRef.get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            DocumentSnapshot userDocument = userTask.getResult();
                            if (userDocument.exists()) {
                                String userFCMToken = userDocument.getString("fcmToken");
                                if (userFCMToken != null && !userFCMToken.isEmpty()) {
                                    // Send push notification to the user
                                    sendPushNotification(userFCMToken, "Double Park Alert", "Your car is being block by another user.");

                                }
                            }
                        } else {
                            // Handle errors in retrieving user document
                            Toast.makeText(Slot_Selection.this, "Failed to retrieve user document: " + userTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, userTask.getException().getMessage());
                        }
                    });




                    String notifType = "doubleParkAlert";

                    // Save user information
                    Map<String, Object> notifMap = new HashMap<>();
                    notifMap.put("matricNo", matricNo);
                    notifMap.put("phone", phone);
                    notifMap.put("blockCarPlate", blockCarPlate);
                    notifMap.put("notifType", notifType);

                    userRef.collection("notifications").add(notifMap)
                            .addOnSuccessListener(documentReference -> {
                                // Notification successfully added
                                // You can perform any additional actions here if needed
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors in adding notification
                                Toast.makeText(Slot_Selection.this, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, e.getMessage());
                            });

                    break;
                }


            } else {
                // Handle errors in the query
                Toast.makeText(Slot_Selection.this, "Query failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, task.getException().getMessage());
            }
        });
    }

    private int compareTimes(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date date1 = sdf.parse(time1);
            Date date2 = sdf.parse(time2);
            return date1.compareTo(date2); // Show error if date 1 earlier than date 2
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Return 0 for any error
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // Method to get the current time
    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        // Set the time zone to the default time zone
        timeFormat.setTimeZone(TimeZone.getDefault());
        return timeFormat.format(new Date());
    }

    private int convertDPToPx(int dp){
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void addNewBlockedCarInput(){
        String hint = "Car Plate " + ++blockedCarPlateCounter;
        // Create a new instance of TextInputLayout
        TextInputLayout newTextInputLayout = new TextInputLayout(this);
        newTextInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        newTextInputLayout.setId(View.generateViewId());  // Generate a unique ID
        newTextInputLayout.setHint(hint);
        newTextInputLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        newTextInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(convertDPToPx(30), convertDPToPx(10), convertDPToPx(30), 0);

        newTextInputLayout.setLayoutParams(layoutParams);

        // Create a new instance of TextInputEditText
        TextInputEditText newTextInputEditText = new TextInputEditText(newTextInputLayout.getContext());
        newTextInputEditText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        newTextInputEditText.setSingleLine(true);


        // Add the new TextInputEditText to the new TextInputLayout
        newTextInputLayout.addView(newTextInputEditText);

        // Add the new TextInputLayout to the parent layout
        blockedCarInputGroup.addView(newTextInputLayout);

        blockedCarList.add(newTextInputLayout);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String text = adapterView.getItemAtPosition(i).toString();
//        Toast.makeText(adapterView.getContext(), text, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void popTimePicker(TextInputLayout view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                view.getEditText().setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,onTimeSetListener,hour,minute, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }
}

