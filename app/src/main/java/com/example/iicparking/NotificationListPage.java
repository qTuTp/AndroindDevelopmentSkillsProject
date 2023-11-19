package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.iicparking.Class.Notification;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class NotificationListPage extends AppCompatActivity implements RecyclerViewInterface{
    private RequestQueue requestQueue;
    private final String TAG = "NotificationListPage0";
    private RecyclerView recyclerView;
    List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_notification);


        setReference();
        setupRecyclerView();
        fetchNotificationsFromFirebase();
    }

    private void setupRecyclerView() {
        Noti_RecyclerAdapter adapter = new Noti_RecyclerAdapter(this, notificationList, this);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void fetchNotificationsFromFirebase() {
        SharedPreferences prefs = getSharedPreferences("UserDataPrefs", Context.MODE_PRIVATE);
        String matricNo = prefs.getString("matriculationNo", "");
        db.collection("users").document(matricNo).collection("notifications").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                notificationList.clear(); // Clear existing data

                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Retrieve notification data from Firestore
                    String notifType = document.getString("notifType");
                    String blockMatricNo = document.getString("matricNo");
                    String date = document.getString("date");
                    String time = document.getString("time");
                    String phone = document.getString("phone");
                    String blockCarPlate = document.getString("blockCarPlate");

                    // Create Notification object and add to the list
                    notificationList.add(new Notification(blockMatricNo, blockCarPlate, phone, date, time, notifType));
                }

                // Notify the adapter that the data set has changed
                recyclerView.getAdapter().notifyDataSetChanged();
            } else {
                // Handle errors in the query
                Toast.makeText(NotificationListPage.this, "Query failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, task.getException().getMessage());
            }
        });

    }
    private void setReference(){
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.notificationRecyclerView);
        requestQueue = Volley.newRequestQueue(this);
    }

    private void showNotificationDialog(Notification notification) {
        if(notification.getNotifType().equals("relocateAlert")){
            Dialog relocateDialog = new Dialog(this);
            relocateDialog.setContentView(R.layout.noti_pop1);
            relocateDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            relocateDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
            relocateDialog.setCancelable(true);


            relocateDialog.show();
        }else if (notification.getNotifType().equals("doubleParkAlert")){
            Dialog doubleParkAlertDialog = new Dialog(this);
            doubleParkAlertDialog.setContentView(R.layout.activity_alert_pop_up);
            doubleParkAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            doubleParkAlertDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_box));
            doubleParkAlertDialog.setCancelable(true);

            TextView matricNo = doubleParkAlertDialog.findViewById(R.id.alert_id);
            TextView blockCarPlate = doubleParkAlertDialog.findViewById(R.id.alert_blockCarPlate);
            TextView phone = doubleParkAlertDialog.findViewById(R.id.alert_phoneNo);
            TextView time = doubleParkAlertDialog.findViewById(R.id.alert_Time);

            AppCompatButton notifyUser = doubleParkAlertDialog.findViewById(R.id.alert_notifyButton);

            matricNo.setText(notification.getMatricNo());
            blockCarPlate.setText(notification.getBlockCarPlate());
            phone.setText(notification.getPhone());
            time.setText(notification.getDate() + " " + notification.getTime());

            notifyUser.setOnClickListener(view -> {
                DocumentReference userRef = db.collection("users").document(notification.getMatricNo());

                //Send push notification to user
                // Retrieve FCM token from the user document
                userRef.get().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        DocumentSnapshot userDocument = userTask.getResult();
                        if (userDocument.exists()) {
                            String userFCMToken = userDocument.getString("fcmToken");
                            if (userFCMToken != null && !userFCMToken.isEmpty()) {
                                // Send push notification to the user
                                sendPushNotification(userFCMToken, "Relocate Alert", "Please Move Your Car Immediately");

                            }
                        }
                    } else {
                        // Handle errors in retrieving user document
                        Toast.makeText(NotificationListPage.this, "Failed to retrieve user document: " + userTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, userTask.getException().getMessage());
                    }
                });

                String notifType = "relocateAlert";

                // Save user information
                Map<String, Object> notifMap = new HashMap<>();

                notifMap.put("notifType", notifType);
                notifMap.put("date", getCurrentDate());
                notifMap.put("time", getCurrentTime());

                db.collection("users").document(notification.getMatricNo()).collection("notifications").add(notifMap)
                        .addOnSuccessListener(documentReference -> {
                            // Notification successfully added
                            // You can perform any additional actions here if needed
                            doubleParkAlertDialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            // Handle errors in adding notification
                            Toast.makeText(NotificationListPage.this, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.getMessage());
                            doubleParkAlertDialog.dismiss();
                        });

            });

            doubleParkAlertDialog.show();

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
                        Toast.makeText(NotificationListPage.this, "Request error", Toast.LENGTH_LONG).show();
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

    private void showAlertPopUpDialog (){

    }

    @Override
    public void onItemClick(int position) {
        Notification clickedNotification = notificationList.get(position);
        showNotificationDialog(clickedNotification);
    }
}