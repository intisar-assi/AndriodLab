package com.example.a1211905_1200530_courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText, confirmPasswordEditText, phoneEditText;
    private Spinner genderSpinner, citySpinner;
    private Button registerButton;
    private TextView loginLink;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        genderSpinner = findViewById(R.id.genderSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Setup spinners
        setupSpinners();

        // Set click listeners
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupSpinners() {
        // Gender spinner
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // City spinner
        String[] cities = {"Ramallah", "Bethlehem", "Nablus", "Hebron", "Jenin", "Tulkarm", "Qalqilya", "Jericho", "Gaza", "Other"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        // Set city spinner listener for phone number auto-fill
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = parent.getItemAtPosition(position).toString();
                autoFillPhoneNumber(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void autoFillPhoneNumber(String city) {
        String countryCode = "+970";

        String phonePrefix = countryCode;
        String currentPhone = phoneEditText.getText().toString();
        
        // Only auto-fill if the field is empty or doesn't start with the correct prefix
        if (TextUtils.isEmpty(currentPhone) || !currentPhone.startsWith(phonePrefix)) {
            phoneEditText.setText(phonePrefix);
            phoneEditText.setSelection(phonePrefix.length());
        }
    }

    private void performRegistration() {
        String email = emailEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String city = citySpinner.getSelectedItem().toString();

        // Validate inputs
        if (!validateInputs(email, firstName, lastName, password, confirmPassword, phone)) {
            return;
        }

        // Check if email already exists
        if (databaseHelper.isEmailExists(email)) {
            emailEditText.setError(getString(R.string.email_exists));
            emailEditText.requestFocus();
            return;
        }

        // Add user to database
        long userId = databaseHelper.addUser(email, firstName, lastName, password, gender, city, phone);
        
        if (userId > 0) {
            Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String email, String firstName, String lastName, 
                                 String password, String confirmPassword, String phone) {
        // Check if fields are empty
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError("First name is required");
            firstNameEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Please confirm your password");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            phoneEditText.requestFocus();
            return false;
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.invalid_email));
            emailEditText.requestFocus();
            return false;
        }

        // Validate first name length (≥3 characters)
        if (firstName.length() < 3) {
            firstNameEditText.setError("First name must be at least 3 characters");
            firstNameEditText.requestFocus();
            return false;
        }

        // Validate last name length (≥3 characters)
        if (lastName.length() < 3) {
            lastNameEditText.setError("Last name must be at least 3 characters");
            lastNameEditText.requestFocus();
            return false;
        }

        // Validate password length and complexity (≥5 characters, at least 1 letter, 1 number, 1 special character)
        if (password.length() < 5) {
            passwordEditText.setError("Password must be at least 5 characters");
            passwordEditText.requestFocus();
            return false;
        }

        // Check password complexity
        boolean hasLetter = Pattern.compile("[a-zA-Z]").matcher(password).find();
        boolean hasNumber = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();

        if (!hasLetter || !hasNumber || !hasSpecial) {
            passwordEditText.setError("Password must contain at least 1 letter, 1 number, and 1 special character");
            passwordEditText.requestFocus();
            return false;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.passwords_not_match));
            confirmPasswordEditText.requestFocus();
            return false;
        }

        // Validate phone number (basic validation)
        if (phone.length() < 10) {
            phoneEditText.setError("Please enter a valid phone number");
            phoneEditText.requestFocus();
            return false;
        }

        return true;
    }
} 