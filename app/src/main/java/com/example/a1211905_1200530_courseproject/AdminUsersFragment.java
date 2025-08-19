package com.example.a1211905_1200530_courseproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {
    private ListView userListView;
    private DatabaseHelper databaseHelper;
    private List<DatabaseHelper.User> users;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        userListView = view.findViewById(R.id.userListView);
        databaseHelper = new DatabaseHelper(getContext());

        loadUsers();

        return view;
    }

    private void loadUsers() {
        users = databaseHelper.getAllUsers();
        List<String> userStrings = new ArrayList<>();

        for (DatabaseHelper.User user : users) {
            String userInfo = String.format("%s %s\nEmail: %s\nPhone: %s\nCity: %s",
                user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), user.getCity());
            userStrings.add(userInfo);
        }

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, userStrings);
        userListView.setAdapter(adapter);

        // Show user count
        if (getActivity() != null) {
            getActivity().setTitle("View Users (" + users.size() + ")");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers(); // Refresh when returning to fragment
    }
}
