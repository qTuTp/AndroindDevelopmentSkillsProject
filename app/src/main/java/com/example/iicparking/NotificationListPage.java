package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.iicparking.Class.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationListPage extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_notification);
        setReference();

        List<Notification> notificationList = new ArrayList<>();

        notificationList.add(new Notification("Title", "23 Sept 2023 4:45pm", "Testing"));
        notificationList.add(new Notification("Title2", "23 Sept 2023 4:45pm", "Testing"));
        notificationList.add(new Notification("Title3", "23 Sept 2023 4:45pm", "Testing"));
        notificationList.add(new Notification("Title4", "23 Sept 2023 4:45pm", "Testing"));
        notificationList.add(new Notification("Title5", "23 Sept 2023 4:45pm", "Testing"));

        Noti_RecyclerAdapter adapter = new Noti_RecyclerAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        // Set the layout manager (e.g., LinearLayoutManager)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);





    }

    private void setReference(){
        recyclerView = findViewById(R.id.notificationRecyclerView);


    }
}