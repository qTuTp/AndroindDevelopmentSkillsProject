package com.example.iicparking;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SearchVehicle extends AppCompatActivity {

    private Button searchButton;
    private EditText searchField;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_vehicle);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        searchButton = findViewById(R.id.SEARCH);
        searchField = findViewById(R.id.searchbar);

        searchButton.setOnClickListener(view -> {
            String searchTerm = searchField.getText().toString().trim();
            if (!searchTerm.isEmpty()) {
                searchInFirestore(searchTerm);
            } else {
                Toast.makeText(SearchVehicle.this, "Please enter a car plate number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchInFirestore(String searchTerm) {
        Query query = db.collectionGroup("cars").whereEqualTo("carPlateNumber", searchTerm);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    DocumentReference userRef = document.getReference().getParent().getParent();

                    userRef.get().addOnSuccessListener(userDocument -> {
                        if (userDocument.exists()) {
                            String vehicleNumber = document.getString("carPlateNumber");
                            Toast.makeText(SearchVehicle.this, "Vehicle Number found: " + vehicleNumber, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SearchVehicle.this, "User document doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        // Handle failure to fetch user document
                        Toast.makeText(SearchVehicle.this, "Failed to fetch user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                    break;
                }
            } else {
                // Handle errors in the query
                Toast.makeText(SearchVehicle.this, "Query failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
