package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Report extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText inputDescription;
    private AppCompatButton submitButton;

    private FirebaseFirestore db;
    private CollectionReference reportsCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Spinner spinner = findViewById(R.id.spinner_1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.title_issue, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        db = FirebaseFirestore.getInstance();
        reportsCollection = db.collection("Reports");

        inputDescription = findViewById(R.id.inputDescription);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = inputDescription.getText().toString();
                String selectedTitle = spinner.getSelectedItem().toString();

                // Create a map to store data
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("title", selectedTitle);
                reportData.put("description", data);

                // Store data in Firestore
                reportsCollection.add(reportData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(Report.this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                            inputDescription.setText(""); // Clear input after submission if needed
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Report.this, "Failed to submit data", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String text = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(adapterView.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
