package com.example.a1211905_1200530_courseproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrdersFragment extends Fragment {
    private ListView orderListView;
    private DatabaseHelper databaseHelper;
    private List<DatabaseHelper.Order> allOrders;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        orderListView = view.findViewById(R.id.orderListView);
        databaseHelper = new DatabaseHelper(getContext());

        loadAllOrders();

        // Set click listener for order status updates
        orderListView.setOnItemClickListener((parent, view1, position, id) -> {
            DatabaseHelper.Order selectedOrder = allOrders.get(position);
            showOrderStatusDialog(selectedOrder);
        });

        return view;
    }

    private void loadAllOrders() {
        allOrders = databaseHelper.getAllOrders();
        List<String> orderStrings = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        for (DatabaseHelper.Order order : allOrders) {
            String orderInfo = String.format("Order #%d\nProduct: %s\nQuantity: %d\nCustomer ID: %d\nStatus: %s\nDate: %s\nDelivery: %s",
                order.getId(), order.getProductName(), order.getQuantity(), order.getUserId(),
                order.getStatus(), sdf.format(new Date(order.getOrderTime())), order.getDeliveryMethod());
            orderStrings.add(orderInfo);
        }

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, orderStrings);
        orderListView.setAdapter(adapter);

        // Show order count
        if (getActivity() != null) {
            getActivity().setTitle("Manage Orders (" + allOrders.size() + ")");
        }
    }

    private void showOrderStatusDialog(DatabaseHelper.Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_order_status, null);

        Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
        String[] statuses = {"Pending", "Approved", "Delivered", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Set current status
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(order.getStatus())) {
                statusSpinner.setSelection(i);
                break;
            }
        }

        builder.setView(dialogView)
                .setTitle("Update Order Status")
                .setPositiveButton("Update", (dialog, which) -> {
                    String newStatus = statusSpinner.getSelectedItem().toString();
                    if (databaseHelper.updateOrderStatus(order.getId(), newStatus)) {
                        Toast.makeText(getContext(), "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        loadAllOrders(); // Refresh list
                    } else {
                        Toast.makeText(getContext(), "Failed to update order status", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllOrders(); // Refresh when returning to fragment
    }
}
