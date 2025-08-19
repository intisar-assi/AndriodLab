package com.example.a1211905_1200530_courseproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView;
    private EditText firstNameEditText, lastNameEditText, phoneEditText, currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button updateProfileButton, updatePasswordButton, changePhotoButton;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private DatabaseHelper.User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.profileNameTextView);
        emailTextView = view.findViewById(R.id.profileEmailTextView);
        phoneTextView = view.findViewById(R.id.profilePhoneTextView);
        
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        updatePasswordButton = view.findViewById(R.id.updatePasswordButton);
        changePhotoButton = view.findViewById(R.id.changePhotoButton);

        // Initialize database and preferences
        databaseHelper = new DatabaseHelper(getContext());
        sharedPreferences = getActivity().getSharedPreferences("GroceryApp", 0);
        userEmail = sharedPreferences.getString("user_email", "");

        // Load user data
        loadUserData();

        // Set click listeners
        updateProfileButton.setOnClickListener(v -> updateProfile());
        updatePasswordButton.setOnClickListener(v -> updatePassword());
        changePhotoButton.setOnClickListener(v -> selectImage());

        return view;
    }

    private void loadUserData() {
        currentUser = databaseHelper.getUserByEmail(userEmail);
        if (currentUser != null) {
            nameTextView.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            emailTextView.setText(currentUser.getEmail());
            phoneTextView.setText(currentUser.getPhone());
            
            firstNameEditText.setText(currentUser.getFirstName());
            lastNameEditText.setText(currentUser.getLastName());
            phoneEditText.setText(currentUser.getPhone());
        }
    }

    private void updateProfile() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate inputs
        if (!validateProfileInputs(firstName, lastName, phone)) {
            return;
        }

        // Update user in database
        boolean success = databaseHelper.updateUserProfile(userEmail, firstName, lastName, phone);
        if (success) {
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            loadUserData(); // Reload data
        } else {
            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String currentPassword = currentPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Validate inputs
        if (!validatePasswordInputs(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        // Check if current password is correct
        if (!databaseHelper.checkUser(userEmail, currentPassword)) {
            currentPasswordEditText.setError("Current password is incorrect");
            return;
        }

        // Update password in database
        boolean success = databaseHelper.updateUserPassword(userEmail, newPassword);
        if (success) {
            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
            // Clear password fields
            currentPasswordEditText.setText("");
            newPasswordEditText.setText("");
            confirmPasswordEditText.setText("");
        } else {
            Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                profileImageView.setImageURI(imageUri);
                Toast.makeText(getContext(), "Profile photo updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateProfileInputs(String firstName, String lastName, String phone) {
        if (TextUtils.isEmpty(firstName) || firstName.length() < 3) {
            firstNameEditText.setError("First name must be at least 3 characters");
            return false;
        }

        if (TextUtils.isEmpty(lastName) || lastName.length() < 3) {
            lastNameEditText.setError("Last name must be at least 3 characters");
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            return false;
        }

        return true;
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordEditText.setError("Current password is required");
            return false;
        }

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 5) {
            newPasswordEditText.setError("New password must be at least 5 characters");
            return false;
        }

        // Check password complexity (same as registration)
        if (!newPassword.matches(".*[a-zA-Z].*") || !newPassword.matches(".*\\d.*") || !newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            newPasswordEditText.setError("Password must contain at least 1 letter, 1 number, and 1 special character");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return false;
        }

        return true;
    }
} 