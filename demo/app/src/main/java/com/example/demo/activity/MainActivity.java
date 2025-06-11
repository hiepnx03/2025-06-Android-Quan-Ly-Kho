package com.example.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.R;
import com.example.demo.util.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    // UI components
    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogin;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        initializeDatabase();

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeDatabase() {
        new DatabaseHelper(this).getWritableDatabase();
    }

    private void initializeViews() {
        edtUsername = findViewById(R.id.edt_user);
        edtPassword = findViewById(R.id.edt_pass);
        btnLogin = findViewById(R.id.btn_Login);
        btnCancel = findViewById(R.id.btn_Cancel);
    }

    private void setupClickListeners() {
        // Cancel button click handler
        btnCancel.setOnClickListener(v -> showExitConfirmationDialog());

        // Login button click handler
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Do you want to exit the application?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showToast("Please fill in all fields");
            return;
        }

        // Check credentials
        if (username.equalsIgnoreCase("1") && password.equalsIgnoreCase("1")) {
            showToast("Login successful");
            navigateToQlihhActivity();
        } else {
            showToast("Login failed");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToQlihhActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}