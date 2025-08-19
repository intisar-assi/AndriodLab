package com.example.a1211905_1200530_courseproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private Button loginButton;
    private TextView registerLink;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // Initialize database and preferences
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("GroceryApp", MODE_PRIVATE);

        // Check if remember me is enabled
        if (sharedPreferences.getBoolean("remember_me", false)) {
            String savedEmail = sharedPreferences.getString("saved_email", "");
            emailEditText.setText(savedEmail);
            rememberMeCheckBox.setChecked(true);
        }

        // Set click listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Check for admin login
        if (email.equals("admin@admin.com") && password.equals("Admin123!")) {
            // Save admin session
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.putBoolean("is_admin", true);
            editor.putString("user_email", email);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Check user credentials
        if (databaseHelper.checkUser(email, password)) {
            // Save user session
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.putBoolean("is_admin", false);
            editor.putString("user_email", email);

            // Save email if remember me is checked
            if (rememberMeCheckBox.isChecked()) {
                editor.putBoolean("remember_me", true);
                editor.putString("saved_email", email);
            } else {
                editor.putBoolean("remember_me", false);
                editor.remove("saved_email");
            }
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String email, String password) {
        // Check if fields are empty
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.invalid_email));
            emailEditText.requestFocus();
            return false;
        }

        // Validate password length
        if (password.length() < 5) {
            passwordEditText.setError("Password must be at least 5 characters");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is already logged in
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            if (sharedPreferences.getBoolean("is_admin", false)) {
                Intent intent = new Intent(this, AdminActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}