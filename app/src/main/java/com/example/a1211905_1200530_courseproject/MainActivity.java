package com.example.a1211905_1200530_courseproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize preferences and database
        sharedPreferences = getSharedPreferences("GroceryApp", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // Check if user is logged in
        if (!sharedPreferences.getBoolean("is_logged_in", false)) {
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

        // Setup navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_home, R.string.nav_home);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Update navigation header with user info
        updateNavigationHeader();

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userNameText = headerView.findViewById(R.id.userNameText);
        TextView userEmailText = headerView.findViewById(R.id.userEmailText);

        String userEmail = sharedPreferences.getString("user_email", "");
        DatabaseHelper.User user = databaseHelper.getUserByEmail(userEmail);

        if (user != null) {
            userNameText.setText(user.getFirstName() + " " + user.getLastName());
            userEmailText.setText(user.getEmail());
        } else {
            userNameText.setText("User");
            userEmailText.setText(userEmail);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_products) {
            fragment = new ProductsFragment();
        } else if (itemId == R.id.nav_orders) {
            fragment = new OrdersFragment();
        } else if (itemId == R.id.nav_favorites) {
            fragment = new FavoritesFragment();
        } else if (itemId == R.id.nav_offers) {
            fragment = new OffersFragment();
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
        } else if (itemId == R.id.nav_contact) {
            fragment = new ContactFragment();
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

    private void loadFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Fallback to home fragment
            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, new HomeFragment())
                        .commit();
            } catch (Exception ex) {
                // If even home fragment fails, show error
                Toast.makeText(this, "Critical error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void refreshProductsFragment() {
        // Find the current products fragment and refresh it
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof ProductsFragment) {
            ProductsFragment productsFragment = (ProductsFragment) currentFragment;
            // The onResume method will handle the refresh
        }
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

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationHeader();
    }
}