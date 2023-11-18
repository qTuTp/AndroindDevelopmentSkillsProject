package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.iicparking.Class.Notification;
import com.example.iicparking.Class.ParkLog;
import com.example.iicparking.Class.Vehicle;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;



public class HomePage extends AppCompatActivity {

    final String TAG = "HOMEPAGE";

    private AppCompatButton manageParkButton;
    private AppCompatButton manageCarButton;
    private AppCompatButton parkSelectButton;
    private AppCompatButton searchVehicleButton;
    private AppCompatButton reportButton;
    private AppCompatButton profileButton;
    private AppCompatButton peakTimeButton;
    private AppCompatButton notificationButton;
    private ImageButton logoutButton;
    private TextView carPlateNum;
    private ProgressBar floor1;
    private ProgressBar basement1;
    private ProgressBar basement2;
    private ProgressBar garden;
    private ProgressBar loadingIndicator;
    private View loadingOverlay;
    private FirebaseFirestore db;
    private List<Vehicle> userCars;
    private List<String> carPlateList;
    private String currentVehiclePlate;
    int hour, minute;

    //Declare the max vacancy
    private final int FLOOR1_MAX = 83;
    private final int BASEMENT1_MAX = 180;
    private final int BASEMENT2_MAX = 95;
    private final int GARDEN_MAX = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setReference();

//        putDummyDataIntoFirestore();

        fetchUserCarsFromFirestore();
        updateParkingVacancy();




    }

    // Function to add dummy data for testing
    private void putDummyDataIntoFirestore(){
        String currentDate = getCurrentDate();
        ParkLog parkLog1 = new ParkLog("ABC123", "19:30", "Floor 1", "123456", "Normal Parking", "17:30");
        ParkLog parkLog2 = new ParkLog("DEF456", "18:45", "Basement -1", "789012", "Normal Parking", "16:00");
        ParkLog parkLog3 = new ParkLog("GHI789", "20:15", "Garden", "345678", "Normal Parking", "18:30");
        ParkLog parkLog4 = new ParkLog("JKL012", "19:00", "Basement -2", "901234", "Normal Parking", "15:45");
        ParkLog parkLog5 = new ParkLog("MNO345", "17:45", "Floor 1", "567890", "Normal Parking", "14:30");

        ParkLog parkLog6 = new ParkLog("PQR678", "18:30", "Garden", "123123", "Normal Parking", "16:15");
        ParkLog parkLog7 = new ParkLog("STU901", "19:45", "Basement -1", "456456", "Normal Parking", "18:00");
        ParkLog parkLog8 = new ParkLog("VWX234", "20:30", "Floor 1", "789789", "Normal Parking", "19:00");
        ParkLog parkLog9 = new ParkLog("YZA567", "18:15", "Basement -2", "012345", "Normal Parking", "16:45");
        ParkLog parkLog10 = new ParkLog("BCD890", "19:30", "Garden", "678901", "Normal Parking", "17:00");

        ParkLog parkLog11 = new ParkLog("EFG123", "19:15", "Floor 1", "234567", "Normal Parking", "18:00");
        ParkLog parkLog12 = new ParkLog("HIJ456", "18:45", "Garden", "890123", "Normal Parking", "17:30");
        ParkLog parkLog13 = new ParkLog("KLM789", "20:00", "Basement -1", "345678", "Normal Parking", "19:15");
        ParkLog parkLog14 = new ParkLog("NOP012", "19:30", "Floor 1", "901234", "Normal Parking", "18:15");
        ParkLog parkLog15 = new ParkLog("QRS345", "18:00", "Basement -2", "567890", "Normal Parking", "16:30");

        ParkLog parkLog16 = new ParkLog("TUV678", "20:15", "Garden", "123456", "Normal Parking", "19:00");
        ParkLog parkLog17 = new ParkLog("WXY901", "19:45", "Basement -1", "789012", "Normal Parking", "18:30");
        ParkLog parkLog18 = new ParkLog("ZAB234", "18:30", "Floor 1", "345678", "Normal Parking", "17:15");
        ParkLog parkLog19 = new ParkLog("CDE567", "19:00", "Basement -2", "901234", "Normal Parking", "18:45");
        ParkLog parkLog20 = new ParkLog("FGH890", "20:30", "Garden", "567890", "Normal Parking", "19:15");

        List<ParkLog> parkLogs = Arrays.asList(
                parkLog1, parkLog2, parkLog3, parkLog4, parkLog5,
                parkLog6, parkLog7, parkLog8, parkLog9, parkLog10,
                parkLog11, parkLog12, parkLog13, parkLog14, parkLog15,
                parkLog16, parkLog17, parkLog18, parkLog19, parkLog20
        );

        for (ParkLog parkLog : parkLogs) {
            db.collection("parkLog")
                    .document(currentDate)
                    .collection("logs")
                    .add(parkLog)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding document", e);
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateParkingVacancy();
    }

    private void updateParkingVacancy(){
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);

        CollectionReference parkLogCollection = db.collection("parkLog");
        String currentDate = getCurrentDate();
        String currentTime = getCurrentTime();

        parkLogCollection.document(currentDate)
                .collection("logs")
                .whereGreaterThanOrEqualTo("endTime", currentTime)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Initialize a map to store the count for each location
                        Map<String, Integer> parkedCarsByLocation = new HashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Check if the log has an "exited" status
                            String status = document.getString("status");
                            if (status == null || !status.equals("exited")) {
                                // Car is still parked
                                String location = document.getString("location");
                                Log.d(TAG, "Location: " + location + "EndTime: " + document.getString("endTime"));

                                // Update the count for the location
                                int count = parkedCarsByLocation.getOrDefault(location, 0);
                                parkedCarsByLocation.put(location, count + 1);
                            }
                        }

                        // Now, parkedCarsByLocation contains the count for each location
                        for (Map.Entry<String, Integer> entry : parkedCarsByLocation.entrySet()) {
                            String location = entry.getKey();
                            int count = entry.getValue();
                            Log.d(TAG, "Location: " + location + " Count: " + count);
                            // Update the progress bar

                            if(location.equals("Floor 1")){
                                int floor1Percentage = (count*100)/FLOOR1_MAX;
                                updateProgressBar(floor1,floor1Percentage);
                            } else if (location.equals("Basement -1")) {
                                int basement1Percentage = (count*100)/BASEMENT1_MAX;
                                updateProgressBar(basement1,basement1Percentage);
                            } else if (location.equals("Basement -2")){
                                int basement2Percentage = (count*100)/BASEMENT2_MAX;
                                updateProgressBar(basement2,basement2Percentage);
                            } else if (location.equals("Garden")) {
                                int gardenPercentage = (count*100)/GARDEN_MAX;
                                updateProgressBar(garden,gardenPercentage);
                            }

                        }

                        loadingOverlay.setVisibility(View.GONE);
                        loadingIndicator.setVisibility(View.GONE);
                    } else {
                        // Handle errors
                        Exception exception = task.getException();
                        if (exception != null) {
                            // Handle the exception\
                            Log.e(TAG, exception.getMessage().toString());
                        }
                        loadingOverlay.setVisibility(View.GONE);
                        loadingIndicator.setVisibility(View.GONE);
                    }
                });
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date());
    }

    private void showEditVehicleDialog(Vehicle vehicle){
        Dialog editVehicleDialog = new Dialog(this);
        editVehicleDialog.setContentView(R.layout.edit_vehicle);
        editVehicleDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editVehicleDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        editVehicleDialog.setCancelable(true);

        TextInputLayout carPlateNumberText = editVehicleDialog.findViewById(R.id.carPlateNo);
        TextInputLayout carColorText = editVehicleDialog.findViewById(R.id.carColor);

        MaterialButton saveButton = editVehicleDialog.findViewById(R.id.saveButton);
        MaterialButton cancelButton = editVehicleDialog.findViewById(R.id.cancelButton);

        carPlateNumberText.getEditText().setText(vehicle.getCarPlateNumber());
        carColorText.getEditText().setText(vehicle.getColor());

        saveButton.setOnClickListener(view -> {
            String carPlateNumber = carPlateNumberText.getEditText().getText().toString().trim();
            String carColor = carColorText.getEditText().getText().toString().trim();

            // Validation
            if (carPlateNumber.isEmpty()) {
                Log.d(TAG, "Car Plate Number is empty");
                carPlateNumberText.setError("Car Plate Number is Required");
            } else if (carColor.isEmpty()) {
                Log.d(TAG, "Car Color is empty");
                carColorText.setError("Car Color is Required");
            } else {
                Log.d(TAG, "Validation successful");

                SharedPreferences pref = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
                String matricNo = pref.getString("matriculationNo", "");

                CollectionReference carsCollectionRef = db.collection("users").document(matricNo).collection("cars");
                Query query = carsCollectionRef.whereEqualTo("carPlateNumber", vehicle.getCarPlateNumber());

                if (currentVehiclePlate.equals(vehicle.getCarPlateNumber())){
                    db.collection("users").document(matricNo).
                            update("currentVehicle", carPlateNumber).
                            addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Current Vehicle updated. ");
                            }).
                            addOnFailureListener(e -> {
                                Log.d(TAG, "Current Vehicle update fail. ");

                            });
                }

                query.get().addOnCompleteListener(queryTask -> {
                    Log.d(TAG, "Query complete");
                    if (queryTask.isSuccessful()) {
                        Log.d(TAG, "Query successful");
                        for (QueryDocumentSnapshot document : queryTask.getResult()) {
                            Log.d(TAG, "In Loop");
                            // Update the existing document
                            DocumentReference vehicleRef = carsCollectionRef.document(document.getId());

                            // Create a map with the updated data
                            Map<String, Object> updatedData = new HashMap<>();
                            updatedData.put("carPlateNumber", carPlateNumber);
                            updatedData.put("color", carColor);
                            Log.d(TAG, carPlateNumber);

                            // Update the document
                            vehicleRef.update(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully updated the vehicle
                                        // You can perform any additional actions or UI updates here
                                        Log.d(TAG, "Vehicle updated: " + vehicleRef.getId());
                                        Toast.makeText(this, "Vehicle Updated", Toast.LENGTH_SHORT).show();
                                        // Dismiss the dialog
                                        editVehicleDialog.dismiss();
                                        if (currentVehiclePlate.equals(vehicle.getCarPlateNumber())){
                                            currentVehiclePlate = carPlateNumber;
                                        }
                                        fetchUserCarsFromFirestore();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to update the vehicle
                                        // Handle the error, show a toast, or log the error
                                        Log.e(TAG, "Error updating vehicle", e);
                                        Toast.makeText(this, "Error updating vehicle", Toast.LENGTH_SHORT).show();
                                    });
                            // Only update the first matching document (if there are multiple)
                            break;
                        }
                    } else {
                        // Handle errors in the query
                        Log.e(TAG, "Error querying for vehicle", queryTask.getException());
                        Toast.makeText(this, "Error querying for vehicle", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        cancelButton.setOnClickListener(view -> {
            editVehicleDialog.dismiss();
        });

        editVehicleDialog.show();

    }

    private void showAddVehicleDialog(){
        Dialog addVehicleDialog = new Dialog(this);
        addVehicleDialog.setContentView(R.layout.add_new_vehicle);
        addVehicleDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addVehicleDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        addVehicleDialog.setCancelable(true);

        TextInputLayout carPlateNumberText = addVehicleDialog.findViewById(R.id.carPlateNo);
        TextInputLayout carColorText = addVehicleDialog.findViewById(R.id.carColor);

        MaterialButton confirmButton = addVehicleDialog.findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(view -> {
            String carPlateNumber = carPlateNumberText.getEditText().getText().toString().trim();
            String carColor = carColorText.getEditText().getText().toString().trim();

            // Validation
            if (carPlateNumber.isEmpty()){
                carPlateNumberText.setError("Car Plate Number is Required");

            }else if (carColor.isEmpty()){
                carColorText.setError("Car Color is Required");
            } else {
                carPlateNumberText.setError(null);
                carColorText.setError(null);

                SharedPreferences pref = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
                String matricNo = pref.getString("matriculationNo", "");

                Vehicle newVehicle = new Vehicle(carPlateNumber, carColor);

                CollectionReference carsCollectionRef = db.collection("users").document(matricNo).collection("cars");
                carsCollectionRef.add(newVehicle)
                        .addOnSuccessListener(documentReference -> {
                            // Successfully added the new vehicle
                            // You can perform any additional actions or UI updates here
                            Log.d(TAG, "New vehicle added with ID: " + documentReference.getId());
                            Toast.makeText(this, "New Vehicle Added", Toast.LENGTH_SHORT).show();
                            userCars.add(newVehicle);
                            carPlateList.add(newVehicle.getCarPlateNumber());
                            addVehicleDialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            // Failed to add the new vehicle
                            // Handle the error, show a toast, or log the error
                            Log.e(TAG, "Error adding new vehicle", e);
                            Toast.makeText(this, "Error adding new vehicle", Toast.LENGTH_SHORT).show();

                        });


            }
        });

        addVehicleDialog.show();
    }

    private void showManageCarDialog() {
        Dialog manageCarDialog = new Dialog(this);
        manageCarDialog.setContentView(R.layout.manage_vehicle_pop_up);
        manageCarDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        manageCarDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        manageCarDialog.setCancelable(true);

        // Spinner to display user's cars
        Spinner carSpinner = manageCarDialog.findViewById(R.id.carPlateSpinner);

        // Check if car plate list is not empty
        if (carPlateList != null && !carPlateList.isEmpty()) {
            Log.d(TAG, "Car Plate List is not empty: " + carPlateList.get(0) );
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, carPlateList);
            carSpinner.setAdapter(adapter);
        } else {
            // If userCars is empty, add "NONE" to the spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"NONE"});
            carSpinner.setAdapter(adapter);
        }

        // Button to set the selected car as the current vehicle
        MaterialButton setCurrentVehicleButton = manageCarDialog.findViewById(R.id.selectVehicle);
        setCurrentVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentVehiclePlate = (String) carSpinner.getSelectedItem();
                // Set the selected car as the current vehicle (add your logic here)
                Toast.makeText(HomePage.this, "Selected Car: " + currentVehiclePlate, Toast.LENGTH_SHORT).show();

                SharedPreferences pref = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
                String matricNo = pref.getString("matriculationNo", "");

                DocumentReference userRef = db.collection("users").document(matricNo);

                // Update the "currentVehicle" field in Firestore
                userRef.update("currentVehicle", currentVehiclePlate)
                        .addOnSuccessListener(aVoid -> {
                            // Update successful
                            carPlateNum.setText(currentVehiclePlate);
                            manageCarDialog.dismiss();
                            Log.d(TAG, "Current vehicle updated to: " + currentVehiclePlate);
                        })
                        .addOnFailureListener(e -> {
                            // Update failed
                            Log.e(TAG, "Error updating current vehicle", e);
                            Toast.makeText(HomePage.this, "Error updating current vehicle", Toast.LENGTH_SHORT).show();
                        });

            }
        });

        // Button to edit the selected car
        MaterialButton editVehicleButton = manageCarDialog.findViewById(R.id.editVehicle);
        editVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carPlateList != null && !carPlateList.isEmpty()) {
                    String selectedCarPlate = (String) carSpinner.getSelectedItem();
                    Vehicle selectedVehicle = null;
                    for (Vehicle vehicle: userCars){
                        if (vehicle.getCarPlateNumber().equals(selectedCarPlate)){
                            selectedVehicle = vehicle;
                        }
                    }

                    if (selectedVehicle != null){
                        showEditVehicleDialog(selectedVehicle);
                    } else {
                        Toast.makeText(HomePage.this, "Error Occured, Please Try Again...", Toast.LENGTH_SHORT);
                    }

                    manageCarDialog.dismiss();
                } else {
                    // If userCars is empty
                    Toast.makeText(HomePage.this, "You have no vehicle to edit...", Toast.LENGTH_SHORT).show();

                }

            }
        });

        // Button to add a new vehicle
        MaterialButton addNewVehicleButton = manageCarDialog.findViewById(R.id.addVehicle);
        addNewVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVehicleDialog();
                manageCarDialog.dismiss();
            }
        });

        manageCarDialog.show();
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
        logoutButton = findViewById(R.id.logout);

        db = FirebaseFirestore.getInstance();

        loadingIndicator = findViewById(R.id.loadingIndicator);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        userCars = new ArrayList<>();
        carPlateList = new ArrayList<>();

        //progress bar will display tooltip on click
        floor1.setOnClickListener(view -> showTooltipForProgress(floor1));
        basement1.setOnClickListener(view -> showTooltipForProgress(basement1));
        basement2.setOnClickListener(view -> showTooltipForProgress(basement2));
        garden.setOnClickListener(view -> showTooltipForProgress(garden));


        manageParkButton.setOnClickListener(v -> {
            showManageParkDialog();
        });
        manageCarButton.setOnClickListener( v -> {
            showManageCarDialog();

        });
        parkSelectButton.setOnClickListener( v -> {

            //TODO: add validation to check if any current vehicle is selected
            if (currentVehiclePlate != null && !currentVehiclePlate.isEmpty()){

                SharedPreferences pref = getSharedPreferences("ParkPrefs", Context.MODE_PRIVATE);
                String status = pref.getString("status", "");

                if (!status.equals("parked")){
                    //  Add validation to check if the user still park or not
                    Intent intent = new Intent(HomePage.this, Slot_Selection.class);
                    intent.putExtra("currentVehicle", currentVehiclePlate);
                    startActivity(intent);
                } else {
                    Dialog inParkingDialog = new Dialog(this);
                    inParkingDialog.setContentView(R.layout.in_parking_dialog);
                    inParkingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    inParkingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
                    inParkingDialog.setCancelable(true);

                    MaterialButton closeButton = inParkingDialog.findViewById(R.id.closeButton);

                    closeButton.setOnClickListener(view -> {
                        inParkingDialog.dismiss();
                    });

                    inParkingDialog.show();
                }
            }else{
                Dialog noCarSelectedDialog = new Dialog(this);
                noCarSelectedDialog.setContentView(R.layout.no_car_selected_dialog);
                noCarSelectedDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                noCarSelectedDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
                noCarSelectedDialog.setCancelable(true);

                MaterialButton closeButton = noCarSelectedDialog.findViewById(R.id.closeButton);

                closeButton.setOnClickListener(view -> {
                    noCarSelectedDialog.dismiss();
                });

                noCarSelectedDialog.show();
            }



        });
        reportButton.setOnClickListener( v -> {
            Intent intent = new Intent(HomePage.this, Report.class);
            startActivity(intent);
        });
        profileButton.setOnClickListener( v -> {
            Intent intent = new Intent(HomePage.this, Profile.class);
            startActivity(intent);
        });
        searchVehicleButton.setOnClickListener( v -> {
            Intent intent = new Intent(HomePage.this, SearchVehicle.class);
            startActivity(intent);
        });
        peakTimeButton.setOnClickListener( v -> {
            Intent intent = new Intent(HomePage.this, PeakTimePage.class);
            startActivity(intent);
        });
        notificationButton.setOnClickListener( v -> {
            Intent intent = new Intent(HomePage.this, NotificationListPage.class);
            startActivity(intent);
        });
        logoutButton.setOnClickListener( v -> {
            // Logout Confirmation
            Dialog logoutConfirmDialog = new Dialog(this);
            logoutConfirmDialog.setContentView(R.layout.logout_confirm_dialog);
            logoutConfirmDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            logoutConfirmDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
            logoutConfirmDialog.setCancelable(true);

            MaterialButton confirmButton = logoutConfirmDialog.findViewById(R.id.confirmButton);
            MaterialButton cancelButton = logoutConfirmDialog.findViewById(R.id.cancelButton);

            confirmButton.setOnClickListener(view -> {
                SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("matriculationNo", "");
                editor.putString("userName", "");
                editor.putString("email", "");
                editor.putString("phone", "");
                editor.putString("status", "");

                editor.apply();

                Intent intent = new Intent(HomePage.this, LoginPage.class);

                startActivity(intent);
                finish();


            });

            cancelButton.setOnClickListener(view -> {
                logoutConfirmDialog.dismiss();
            });

            logoutConfirmDialog.show();


        });
    }

    private void showManageParkDialog(){
        SharedPreferences prefs = getSharedPreferences("ParkPrefs", Context.MODE_PRIVATE);
        String status = prefs.getString("status", "");
        Log.d(TAG, status);
        // TODO: Need to check if the user have parked or not, if not, show not park yet dialog

        if (status.equals("parked")){
            Dialog manageParkDialog = new Dialog(this);
            manageParkDialog.setContentView(R.layout.park_manage_dialog);
            manageParkDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            manageParkDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
            manageParkDialog.setCancelable(true);


            String carPlateStr = prefs.getString("carPlate", "");
            String startTimeStr = prefs.getString("startTime", "");
            String endTimeStr = prefs.getString("endTime", "");
            String locationStr = prefs.getString("location", "");

            TextView carPlate = manageParkDialog.findViewById(R.id.carPlateNo);
            TextView startTime = manageParkDialog.findViewById(R.id.startTime);
            TextView endTime = manageParkDialog.findViewById(R.id.endTime);
            TextView location = manageParkDialog.findViewById(R.id.location);

            MaterialButton addTimeButton = manageParkDialog.findViewById(R.id.addTimeButton);
            MaterialButton exitParkingButton = manageParkDialog.findViewById(R.id.finishParkingButton);

            addTimeButton.setOnClickListener(view -> {
                showExtendTimeDialog();
                manageParkDialog.dismiss();

            });

            exitParkingButton.setOnClickListener(view -> {
                //Remove parking from local and update the status on firestore to exited
                String documentId = prefs.getString("documentID", "");
                String date = prefs.getString("date", "");

                Log.d(TAG, documentId + " " + date);

                DocumentReference logRef = db.collection("parkLog").document(date).collection("logs").document(documentId);

                logRef.update("status", "exited").
                        addOnSuccessListener(documentPreference -> {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("status", "exited");
                            editor.apply();
                            manageParkDialog.dismiss();

                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating park log status", e);
                            Toast.makeText(this, "Error updating park log status", Toast.LENGTH_SHORT).show();
                            manageParkDialog.dismiss();
                        });


            });
            carPlate.setText(carPlateStr);
            startTime.setText(startTimeStr);
            endTime.setText(endTimeStr);
            location.setText(locationStr);

            manageParkDialog.show();
        }else {
            Dialog notParkedYetDialog = new Dialog(this);
            notParkedYetDialog.setContentView(R.layout.not_park_yet_dialog);
            notParkedYetDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            notParkedYetDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
            notParkedYetDialog.setCancelable(true);

            MaterialButton closeButton = notParkedYetDialog.findViewById(R.id.closeButton);

            closeButton.setOnClickListener(view -> {
                notParkedYetDialog.dismiss();
            });

            notParkedYetDialog.show();
        }



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

    private void showExtendTimeDialog(){
        Dialog extendParkDialog = new Dialog(this);
        extendParkDialog.setContentView(R.layout.extend_park_dialog);
        extendParkDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        extendParkDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        extendParkDialog.setCancelable(true);

        MaterialButton extendTimeButton = extendParkDialog.findViewById(R.id.addTimeButton);
        MaterialButton cancelButton = extendParkDialog.findViewById(R.id.cancelButton);

        TextInputLayout extendTime = extendParkDialog.findViewById(R.id.extendTime);

        TextView startTime = extendParkDialog.findViewById(R.id.startTime);
        TextView endTime = extendParkDialog.findViewById(R.id.endTime);

        SharedPreferences pref = getSharedPreferences("ParkPrefs", Context.MODE_PRIVATE);
        String endTimeStr = pref.getString("endTime", "");
        String startTimeStr = pref.getString("startTime", "");

        startTime.setText(startTimeStr);
        endTime.setText(endTimeStr);

        extendTimeButton.setOnClickListener(v -> {
            extendTime.setError(null);
            //Update the endtime to the extend time, also validate the time
            String extendedTime = extendTime.getEditText().getText().toString().trim();
            if (!extendedTime.isEmpty() && !extendedTime.equals("--:--")){


                if (compareTimes(endTimeStr, extendedTime) > 0){
                    extendTime.setError("Extended Time should be earlier than end time");
                    return;
                }


                String documentId = pref.getString("documentID", "");
                String date = pref.getString("date", "");

                DocumentReference logRef = db.collection("parkLog").document(date).collection("logs").document(documentId);

                logRef.update("endTime", extendedTime).
                        addOnSuccessListener(documentPreference -> {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("endTime", extendedTime);
                            editor.apply();
                            extendParkDialog.dismiss();

                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error Updating Extended Time", e);
                            Toast.makeText(this, "Error Updating Extended Time", Toast.LENGTH_SHORT).show();
                            extendParkDialog.dismiss();
                        });

            }else{
                extendTime.setError("Please select a time");
                return;
            }
        });

        cancelButton.setOnClickListener(v -> {
            extendParkDialog.dismiss();
        });

        extendTime.getEditText().setOnClickListener(v -> {
            popTimePicker(extendTime);
        });

        extendParkDialog.show();
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

    private void fetchUserCarsFromFirestore() {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);

        SharedPreferences pref = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        String matricNo = pref.getString("matriculationNo", "");

        DocumentReference userRef = db.collection("users").document(matricNo);
        Log.d(TAG, "MatricNo: " + matricNo);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Retrieve the current vehicle and list of cars
                    currentVehiclePlate = document.getString("currentVehicle");
                    // Get the "cars" subcollection directly from the user document
                    CollectionReference carsCollectionRef = userRef.collection("cars");

                    carsCollectionRef.get().addOnCompleteListener(carsTask -> {
                        if (carsTask.isSuccessful()) {
                            userCars.clear();
                            carPlateList.clear();
                            for (QueryDocumentSnapshot carDocument : carsTask.getResult()) {
                                Vehicle vehicle = new Vehicle(carDocument.getString("carPlateNumber"),carDocument.getString("color"));
                                userCars.add(vehicle);
                                carPlateList.add(vehicle.getCarPlateNumber());
                            }

                            // Now, userCars contains the list of vehicles in the "cars" subcollection

                            // Set the current vehicle in your UI (e.g., update a TextView)
                            if(currentVehiclePlate == null ){
                                carPlateNum.setText("NONE");
                            }else{
                                carPlateNum.setText(currentVehiclePlate);
                            }

                            loadingOverlay.setVisibility(View.GONE);
                            loadingIndicator.setVisibility(View.GONE);

                            // You can do something with the list, such as updating UI or other logic
                        } else {
                            // Handle errors in fetching "cars" subcollection
                            Log.e(TAG, "Error fetching cars subcollection", carsTask.getException());
                            loadingOverlay.setVisibility(View.GONE);
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    });


                } else {
                    // Handle the case where the document does not exist
                    carPlateNum.setText(R.string.none);
                    loadingOverlay.setVisibility(View.GONE);
                    loadingIndicator.setVisibility(View.GONE);
                }
            } else {
                // Handle errors
                Log.e(TAG, "Error fetching user document", task.getException());
                loadingOverlay.setVisibility(View.GONE);
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation dialog
        Dialog exitConfirmDialog = new Dialog(this);
        exitConfirmDialog.setContentView(R.layout.confirm_exit_dialog);
        exitConfirmDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        exitConfirmDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
        exitConfirmDialog.setCancelable(true);

        MaterialButton exitDialogConfirmButton = exitConfirmDialog.findViewById(R.id.confirmButton);
        MaterialButton exitDialogCancelButton = exitConfirmDialog.findViewById(R.id.cancelButton);

        exitDialogCancelButton.setOnClickListener(view -> {
            exitConfirmDialog.dismiss();
        });

        exitDialogConfirmButton.setOnClickListener(v -> {
            super.onBackPressed();
        });

        exitConfirmDialog.show();
    }
}