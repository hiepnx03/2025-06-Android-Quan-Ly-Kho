package com.example.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.R;

public class HomeActivity extends AppCompatActivity {

    private Button btnAddTransaction;
    private Button btnViewTransactions;
    private Button btnStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo các thành phần giao diện
        initializeViews();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    private void initializeViews() {
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnViewTransactions = findViewById(R.id.btnViewTransactions);
        btnStatistics = findViewById(R.id.btnStatistics);

        if (btnAddTransaction == null || btnViewTransactions == null || btnStatistics == null) {
            throw new IllegalStateException("Một hoặc nhiều Button không được tìm thấy trong layout");
        }
    }

    private void setupClickListeners() {
        btnAddTransaction.setOnClickListener(v -> navigateToActivity(AddPhieuActivity.class));
        btnViewTransactions.setOnClickListener(v -> navigateToActivity(ViewPhieuActivity.class));
        btnStatistics.setOnClickListener(v -> navigateToActivity(StatisticsActivity.class));
    }

    private void navigateToActivity(Class<?> activityClass) {
        if (activityClass != null) {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
        } else {
            throw new IllegalArgumentException("Activity class không hợp lệ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btnAddTransaction = null;
        btnViewTransactions = null;
        btnStatistics = null;
    }
}