package com.example.a1211905_1200530_courseproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrdersFragment extends Fragment {
    private static final String TAG = "OrdersFragment";
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private LinearLayout emptyStateTextView;
    private boolean isFragmentAttached = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_orders, container, false);

            // Initialize views
            recyclerView = view.findViewById(R.id.ordersRecyclerView);
            emptyStateTextView = view.findViewById(R.id.emptyStateTextView);

            // Initialize database and preferences
            databaseHelper = new DatabaseHelper(requireContext());
            sharedPreferences = requireActivity().getSharedPreferences("GroceryApp", 0);
            userEmail = sharedPreferences.getString("user_email", "");

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new OrderAdapter();
            recyclerView.setAdapter(adapter);

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing Orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Return a simple view to prevent crash
            return new View(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentAttached = true;
        
        // Load orders after fragment is attached
        loadOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentAttached = false;
    }

    private void loadOrders() {
        try {
            if (!isFragmentAttached || getContext() == null) return;

            Log.d(TAG, "Loading orders for user: " + userEmail);
            List<DatabaseHelper.Order> orders = databaseHelper.getUserOrders(userEmail);
            
            if (orders == null || orders.isEmpty()) {
                Log.d(TAG, "No orders found");
                recyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "Found " + orders.size() + " orders");
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateTextView.setVisibility(View.GONE);
                adapter.setOrders(orders);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading orders: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Show empty state on error
            if (recyclerView != null && emptyStateTextView != null) {
                recyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private String formatDateTime(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage(), e);
            return "Unknown Date";
        }
    }

    // Order Adapter
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<DatabaseHelper.Order> orders;

        public void setOrders(List<DatabaseHelper.Order> orders) {
            this.orders = orders;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            try {
                if (orders != null && position < orders.size()) {
                    DatabaseHelper.Order order = orders.get(position);
                    holder.bind(order);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error binding order: " + e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            return orders != null ? orders.size() : 0;
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            private TextView productNameTextView, quantityTextView, dateTimeTextView, statusTextView, deliveryMethodTextView;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.orderProductNameTextView);
                quantityTextView = itemView.findViewById(R.id.orderQuantityTextView);
                dateTimeTextView = itemView.findViewById(R.id.orderDateTimeTextView);
                statusTextView = itemView.findViewById(R.id.orderStatusTextView);
                deliveryMethodTextView = itemView.findViewById(R.id.orderDeliveryMethodTextView);
            }

            public void bind(DatabaseHelper.Order order) {
                try {
                    if (order == null) return;

                    if (productNameTextView != null) {
                        productNameTextView.setText(order.getProductName() != null ? order.getProductName() : "Unknown Product");
                    }
                    if (quantityTextView != null) {
                        quantityTextView.setText("Quantity: " + order.getQuantity());
                    }
                    if (dateTimeTextView != null) {
                        dateTimeTextView.setText("Date: " + formatDateTime(order.getOrderTime()));
                    }
                    if (statusTextView != null) {
                        String status = order.getStatus() != null ? order.getStatus() : "Pending";
                        statusTextView.setText("Status: " + status);
                        
                        // Set status color
                        switch (status.toLowerCase()) {
                            case "pending":
                                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                                break;
                            case "approved":
                                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                                break;
                            case "delivered":
                                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                break;
                            default:
                                statusTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                break;
                        }
                    }
                    if (deliveryMethodTextView != null) {
                        deliveryMethodTextView.setText("Delivery: " + (order.getDeliveryMethod() != null ? order.getDeliveryMethod() : "Not specified"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error binding order data: " + e.getMessage(), e);
                }
            }
        }
    }
} 