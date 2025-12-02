package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 添加按钮点击事件
        Button btnCalculator = findViewById(R.id.btnCalculator);
        Button btnTravel = findViewById(R.id.btnTravel);
        Button btnTableMenu = findViewById(R.id.btnTableMenu);
        Button btnLinearLayout = findViewById(R.id.btnLinearLayout);

        btnCalculator.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalculatorActivity.class);
            startActivity(intent);
        });

        btnTravel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TravelActivity.class);
            startActivity(intent);
        });

        btnTableMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TableMenuActivity.class);
            startActivity(intent);
        });

        btnLinearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LinearLayoutActivity.class);
            startActivity(intent);
        });
    }
}