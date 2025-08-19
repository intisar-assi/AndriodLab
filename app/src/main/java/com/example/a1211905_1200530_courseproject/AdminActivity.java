package com.example.a1211905_1200530_courseproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize preferences and database
        sharedPreferences = getSharedPreferences("GroceryApp", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // Check if user is admin
        if (!sharedPreferences.getBoolean("is_admin", false)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin Panel");

        // Setup navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.admin_title, R.string.admin_title);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Update navigation header
        updateNavigationHeader();

        // Load default fragment (View Users)
        if (savedInstanceState == null) {
            loadFragment(new AdminUsersFragment());
            navigationView.setCheckedItem(R.id.nav_view_users);
        }
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userNameText = headerView.findViewById(R.id.userNameText);
        TextView userEmailText = headerView.findViewById(R.id.userEmailText);

        String userEmail = sharedPreferences.getString("user_email", "");
        userNameText.setText("Admin");
        userEmailText.setText(userEmail);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_delete_customers) {
            showDeleteCustomersDialog();
            return true;
        } else if (itemId == R.id.nav_add_admin) {
            showAddAdminDialog();
            return true;
        } else if (itemId == R.id.nav_manage_products) {
            fragment = new AdminProductsFragment();
        } else if (itemId == R.id.nav_manage_orders) {
            fragment = new AdminOrdersFragment();
        } else if (itemId == R.id.nav_view_users) {
            fragment = new AdminUsersFragment();
        } else if (itemId == R.id.nav_logout) {
            logout();
            return true;
        }

        if (fragment != null) {
            loadFragment(fragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDeleteCustomersDialog() {
        List<DatabaseHelper.User> users = databaseHelper.getAllUsers();
        if (users.isEmpty()) {
            Toast.makeText(this, "No customers to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Filter out admin users (don't allow deletion of admins)
        List<DatabaseHelper.User> customersOnly = new ArrayList<>();
        for (DatabaseHelper.User user : users) {
            if (!user.getEmail().equals("admin@admin.com")) {
                customersOnly.add(user);
            }
        }

        if (customersOnly.isEmpty()) {
            Toast.makeText(this, "No customers to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] userNames = new String[customersOnly.size()];
        for (int i = 0; i < customersOnly.size(); i++) {
            DatabaseHelper.User user = customersOnly.get(i);
            userNames[i] = user.getFirstName() + " " + user.getLastName() + "\nEmail: " + user.getEmail() + "\nPhone: " + user.getPhone() + "\nCity: " + user.getCity();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Customer")
                .setItems(userNames, (dialog, which) -> {
                    DatabaseHelper.User selectedUser = customersOnly.get(which);
                    confirmDeleteUser(selectedUser);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteUser(DatabaseHelper.User user) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete " + user.getFirstName() + " " + user.getLastName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteUser(user.getId())) {
                        Toast.makeText(this, getString(R.string.user_deleted), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_admin, null);

        EditText emailEdit = dialogView.findViewById(R.id.adminEmailEdit);
        EditText firstNameEdit = dialogView.findViewById(R.id.adminFirstNameEdit);
        EditText lastNameEdit = dialogView.findViewById(R.id.adminLastNameEdit);
        EditText passwordEdit = dialogView.findViewById(R.id.adminPasswordEdit);
        EditText confirmPasswordEdit = dialogView.findViewById(R.id.adminConfirmPasswordEdit);
        Spinner genderSpinner = dialogView.findViewById(R.id.adminGenderSpinner);
        Spinner citySpinner = dialogView.findViewById(R.id.adminCitySpinner);
        EditText phoneEdit = dialogView.findViewById(R.id.adminPhoneEdit);

        // Setup gender spinner
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Setup city spinner
        String[] cities = {"Ramallah", "Bethlehem", "Nablus", "Hebron", "Jerusalem" };
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        builder.setView(dialogView)
                .setTitle("Add New Admin")
                .setPositiveButton("Add", (dialog, which) -> {
                    String email = emailEdit.getText().toString();
                    String firstName = firstNameEdit.getText().toString();
                    String lastName = lastNameEdit.getText().toString();
                    String password = passwordEdit.getText().toString();
                    String confirmPassword = confirmPasswordEdit.getText().toString();
                    String gender = genderSpinner.getSelectedItem().toString();
                    String city = citySpinner.getSelectedItem().toString();
                    String phone = phoneEdit.getText().toString();

                    // Validation
                    if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                        password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (firstName.length() < 3 || lastName.length() < 3) {
                        Toast.makeText(this, "First and last names must be at least 3 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (password.length() < 5) {
                        Toast.makeText(this, "Password must be at least 5 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check password complexity (at least 1 letter, 1 number, 1 special character)
                    if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                        Toast.makeText(this, "Password must contain at least 1 letter, 1 number, and 1 special character", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!password.equals(confirmPassword)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (databaseHelper.isEmailExists(email)) {
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add admin user
                    long result = databaseHelper.addUser(email, firstName, lastName, password, gender, city, phone);
                    if (result != -1) {
                        Toast.makeText(this, "Admin added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void logout() {
        // Clear session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Remove placeholder fragment classes - now using separate files
}