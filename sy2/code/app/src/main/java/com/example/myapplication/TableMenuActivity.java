package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TableMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_menu);

        // 这里可以添加菜单项的点击事件
        // 由于是TableLayout演示，主要展示布局效果
        Toast.makeText(this, "Table Layout Menu Demo", Toast.LENGTH_SHORT).show();
    }
}