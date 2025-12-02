package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TravelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        Button btnOneWay = findViewById(R.id.btnOneWay);
        Button btnTraveler = findViewById(R.id.btnTraveler);
        Button btnDepart = findViewById(R.id.btnDepart);

        btnOneWay.setOnClickListener(v -> 
            Toast.makeText(this, "One Way selected", Toast.LENGTH_SHORT).show()
        );

        btnTraveler.setOnClickListener(v -> 
            Toast.makeText(this, "Traveler options", Toast.LENGTH_SHORT).show()
        );

        btnDepart.setOnClickListener(v -> 
            Toast.makeText(this, "Ready to depart to Mars!", Toast.LENGTH_SHORT).show()
        );
    }
}