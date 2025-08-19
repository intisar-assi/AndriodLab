package com.example.a1211905_1200530_courseproject;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoritesFragment extends Fragment {
    private static final String TAG = "FavoritesFragment";
    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private LinearLayout emptyStateTextView;
    private boolean isFragmentAttached = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_favorites, container, false);

            // Initialize views
            recyclerView = view.findViewById(R.id.favoritesRecyclerView);
            emptyStateTextView = view.findViewById(R.id.emptyStateTextView);

            // Initialize database and preferences
            databaseHelper = new DatabaseHelper(requireContext());
            sharedPreferences = requireActivity().getSharedPreferences("GroceryApp", 0);
            userEmail = sharedPreferences.getString("user_email", "");

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new FavoriteAdapter();
            recyclerView.setAdapter(adapter);

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing Favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Return a simple view to prevent crash
            return new View(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentAttached = true;
        // Load favorites after fragment is attached
        loadFavorites();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentAttached = false;
    }

    private void loadFavorites() {
        try {
            if (!isFragmentAttached || getContext() == null) return;

            Log.d(TAG, "Loading favorites for user: " + userEmail);
            List<DatabaseHelper.Favorite> favorites = databaseHelper.getUserFavorites(userEmail);

            if (favorites == null || favorites.isEmpty()) {
                Log.d(TAG, "No favorites found");
                recyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "Found " + favorites.size() + " favorites");
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateTextView.setVisibility(View.GONE);
                adapter.setFavorites(favorites);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorites: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error loading favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Show empty state on error
            if (recyclerView != null && emptyStateTextView != null) {
                recyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    // Favorite Adapter
    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
        private List<DatabaseHelper.Favorite> favorites;

        public void setFavorites(List<DatabaseHelper.Favorite> favorites) {
            this.favorites = favorites;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
            return new FavoriteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
            try {
                if (favorites != null && position < favorites.size()) {
                    DatabaseHelper.Favorite favorite = favorites.get(position);
                    holder.bind(favorite);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error binding favorite: " + e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            return favorites != null ? favorites.size() : 0;
        }

        class FavoriteViewHolder extends RecyclerView.ViewHolder {
            private TextView productNameTextView, priceTextView;
            private ImageButton removeButton;
            private Button orderButton;

            public FavoriteViewHolder(@NonNull View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.favoriteProductNameTextView);
                priceTextView = itemView.findViewById(R.id.favoritePriceTextView);
                removeButton = itemView.findViewById(R.id.removeFavoriteButton);
                orderButton = itemView.findViewById(R.id.orderFromFavoriteButton);
            }

            public void bind(DatabaseHelper.Favorite favorite) {
                try {
                    if (favorite == null) return;

                    if (productNameTextView != null) {
                        productNameTextView.setText(favorite.getProductName() != null ? favorite.getProductName() : "Unknown Product");
                    }
                    if (priceTextView != null) {
                        priceTextView.setText("$" + String.format("%.2f", favorite.getPrice()));
                    }

                    // Remove from favorites button with animation
                    if (removeButton != null) {
                        removeButton.setOnClickListener(v -> {
                            try {
                                if (!isFragmentAttached || getContext() == null) return;

                                // Fade out animation
                                Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out);
                                fadeOut.setDuration(300);
                                itemView.startAnimation(fadeOut);

                                int productId = Integer.parseInt(favorite.getProductId());
                                boolean success = databaseHelper.removeFromFavorites(userEmail, productId);

                                if (success) {
                                    Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                                    loadFavorites(); // Reload the list
                                } else {
                                    Toast.makeText(requireContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                                }
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing product ID: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Error: Invalid product ID", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Error removing favorite: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Error removing from favorites", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // Order button with bounce animation
                    if (orderButton != null) {
                        orderButton.setOnClickListener(v -> {
                            try {
                                if (!isFragmentAttached || getContext() == null) return;

                                // Bounce animation
                                Animation bounce = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
                                bounce.setDuration(200);
                                orderButton.startAnimation(bounce);

                                showOrderDialog(favorite);
                            } catch (Exception e) {
                                Log.e(TAG, "Error in order button click: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Error opening order dialog", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error binding favorite data: " + e.getMessage(), e);
                }
            }

            private void showOrderDialog(DatabaseHelper.Favorite favorite) {
                try {
                    if (!isFragmentAttached || getContext() == null) return;

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_order, null);
                    builder.setView(dialogView);

                    EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
                    AutoCompleteTextView deliverySpinner = dialogView.findViewById(R.id.deliverySpinner);
                    Button confirmButton = dialogView.findViewById(R.id.confirmOrderButton);

                    // Setup delivery spinner
                    String[] deliveryMethods = {"Home Delivery", "Pickup"};
                    ArrayAdapter<String> deliveryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, deliveryMethods);
                    deliverySpinner.setAdapter(deliveryAdapter);
                    deliverySpinner.setText(deliveryMethods[0], false);

                    AlertDialog dialog = builder.create();

                    confirmButton.setOnClickListener(v -> {
                        try {
                            if (!isFragmentAttached || getContext() == null) return;

                            String quantityStr = quantityEditText.getText().toString();
                            if (quantityStr.isEmpty()) {
                                quantityEditText.setError("Please enter quantity");
                                return;
                            }

                            int quantity = Integer.parseInt(quantityStr);
                            if (quantity <= 0) {
                                quantityEditText.setError("Quantity must be greater than 0");
                                return;
                            }

                            String deliveryMethod = deliverySpinner.getText().toString();
                            if (deliveryMethod.isEmpty()) {
                                deliveryMethod = "Home Delivery";
                            }

                            // Save order to database
                            try {
                                int productId = Integer.parseInt(favorite.getProductId());
                                long orderId = databaseHelper.addOrder(userEmail, productId, favorite.getProductName(), quantity, deliveryMethod);

                                if (orderId > 0) {
                                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to place order", Toast.LENGTH_SHORT).show();
                                }
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing product ID: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Error: Invalid product ID", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            quantityEditText.setError("Please enter a valid number");
                        } catch (Exception e) {
                            Log.e(TAG, "Error in confirm order: " + e.getMessage(), e);
                            Toast.makeText(requireContext(), "Error placing order", Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.show();
                } catch (Exception e) {
                    Log.e(TAG, "Error showing order dialog: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error opening order dialog", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}