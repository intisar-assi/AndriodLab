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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OffersFragment extends Fragment {
    private static final String TAG = "OffersFragment";
    private RecyclerView recyclerView;
    private OfferAdapter adapter;
    private List<Product> allProducts;
    private List<Product> offerProducts;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private LinearLayout emptyStateTextView;
    private boolean isFragmentAttached = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_offers, container, false);

            // Initialize views
            recyclerView = view.findViewById(R.id.offersRecyclerView);
            emptyStateTextView = view.findViewById(R.id.emptyStateTextView);

            // Initialize database and preferences
            databaseHelper = new DatabaseHelper(requireContext());
            sharedPreferences = requireActivity().getSharedPreferences("GroceryApp", 0);
            userEmail = sharedPreferences.getString("user_email", "");

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            allProducts = new ArrayList<>();
            offerProducts = new ArrayList<>();
            adapter = new OfferAdapter(offerProducts);
            recyclerView.setAdapter(adapter);

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing Offers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Return a simple view to prevent crash
            return new View(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentAttached = true;
        
        // Load offers after fragment is attached
        loadOffers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentAttached = false;
    }

    private void loadOffers() {
        try {
            if (!isFragmentAttached || getContext() == null) return;

            Log.d(TAG, "Loading offers...");
            
            // Initialize Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://68924ed4447ff4f11fbfdeb4.mockapi.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<Product[]> call = apiService.getProducts();

            call.enqueue(new Callback<Product[]>() {
                @Override
                public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                    try {
                        if (!isFragmentAttached || getContext() == null) return;

                        if (response.isSuccessful() && response.body() != null) {
                            allProducts = Arrays.asList(response.body());
                            Log.d(TAG, "Total products loaded from API: " + allProducts.size());
                            
                            // Log all products to see their offer status
                            for (Product product : allProducts) {
                                Log.d(TAG, "Product: " + product.getName() + ", Offer: " + product.isOffer());
                            }
                            
                            // Filter products with offers
                            offerProducts = allProducts.stream()
                                    .filter(Product::isOffer)
                                    .collect(Collectors.toList());
                            
                            Log.d(TAG, "Found " + offerProducts.size() + " offers from API");
                            
                            // If no offers found from API, load dummy offers
                            if (offerProducts.isEmpty()) {
                                Log.d(TAG, "No offers found from API, loading dummy offers");
                                loadDummyOffers();
                            } else {
                                updateUI();
                            }
                        } else {
                            Log.e(TAG, "API response not successful: " + response.code());
                            loadDummyOffers();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing API response: " + e.getMessage(), e);
                        loadDummyOffers();
                    }
                }

                @Override
                public void onFailure(Call<Product[]> call, Throwable t) {
                    try {
                        if (!isFragmentAttached || getContext() == null) return;
                        
                        Log.e(TAG, "API call failed: " + t.getMessage(), t);
                        loadDummyOffers();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onFailure: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadOffers: " + e.getMessage(), e);
            loadDummyOffers();
        }
    }

    private void updateUI() {
        try {
            if (!isFragmentAttached || getContext() == null) return;

            Log.d(TAG, "updateUI called - offerProducts size: " + offerProducts.size());

            if (offerProducts.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing empty state");
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateTextView.setVisibility(View.GONE);
                
                // Update the adapter's data list
                adapter.updateProducts(offerProducts);
                
                Log.d(TAG, "Updated adapter with " + offerProducts.size() + " offers");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
        }
    }

    private void loadDummyOffers() {
        try {
            if (!isFragmentAttached || getContext() == null) return;

            Log.d(TAG, "Loading dummy offers");
            offerProducts = new ArrayList<>();
            
            // Add dummy offer products with realistic images
            offerProducts.add(new Product("1", "Fruits", "Fresh Organic Apples", 2.99, 50, "https://images.pexels.com/photos/588587/pexels-photo-588587.jpeg?cs=srgb&dl=apple-blur-bright-588587.jpg&fm=jpg", true));
            offerProducts.add(new Product("2", "Vegetables", "Organic Tomatoes", 1.99, 30, "https://images.pexels.com/photos/1327838/pexels-photo-1327838.jpeg?cs=srgb&dl=close-up-fresh-tomatoes-1327838.jpg&fm=jpg", true));
            offerProducts.add(new Product("3", "Dairy", "Fresh Whole Milk", 3.49, 25, "https://images.pexels.com/photos/248412/pexels-photo-248412.jpeg?cs=srgb&dl=agriculture-animal-animal-husbandry-248412.jpg&fm=jpg", true));
            offerProducts.add(new Product("4", "Bakery", "Fresh Baked Bread", 2.49, 20, "https://images.pexels.com/photos/144569/pexels-photo-144569.jpeg?cs=srgb&dl=baked-bread-baking-bread-144569.jpg&fm=jpg", true));
            offerProducts.add(new Product("5", "Fruits", "Sweet Bananas", 1.79, 40, "https://images.pexels.com/photos/47305/bananas-banana-yellow-fruit-47305.jpeg?cs=srgb&dl=banana-bananas-fruit-yellow-47305.jpg&fm=jpg", true));
            offerProducts.add(new Product("6", "Vegetables", "Fresh Carrots", 1.49, 35, "https://images.pexels.com/photos/143133/pexels-photo-143133.jpeg?cs=srgb&dl=agriculture-carrot-close-up-143133.jpg&fm=jpg", true));
            
            Log.d(TAG, "Loaded " + offerProducts.size() + " dummy offers");
            updateUI();
        } catch (Exception e) {
            Log.e(TAG, "Error loading dummy offers: " + e.getMessage(), e);
        }
    }

    public interface ApiService {
        @retrofit2.http.GET("products")
        Call<Product[]> getProducts();
    }

    private class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
        private List<Product> products;

        public OfferAdapter(List<Product> products) {
            this.products = products;
        }

        public void updateProducts(List<Product> newProducts) {
            Log.d(TAG, "OfferAdapter updateProducts called with " + newProducts.size() + " products");
            this.products.clear();
            this.products.addAll(newProducts);
            notifyDataSetChanged();
            Log.d(TAG, "OfferAdapter notifyDataSetChanged called");
        }

        @NonNull
        @Override
        public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
            return new OfferViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
            try {
                Log.d(TAG, "OfferAdapter onBindViewHolder called for position: " + position);
                if (products != null && position < products.size()) {
                    Product product = products.get(position);
                    Log.d(TAG, "Binding product: " + product.getName());
                    holder.bind(product);
                } else {
                    Log.e(TAG, "Invalid position or products list: position=" + position + ", products size=" + (products != null ? products.size() : "null"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error binding offer: " + e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            int count = products != null ? products.size() : 0;
            Log.d(TAG, "OfferAdapter getItemCount called: " + count);
            return count;
        }

        class OfferViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTextView, categoryTextView, priceTextView, stockTextView, offerBadgeTextView;
            private ImageButton favoriteButton;
            private Button orderButton;
            private View productImage;

            public OfferViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.offerProductNameTextView);
                categoryTextView = itemView.findViewById(R.id.offerProductCategoryTextView);
                priceTextView = itemView.findViewById(R.id.offerProductPriceTextView);
                stockTextView = itemView.findViewById(R.id.offerProductStockTextView);
                offerBadgeTextView = itemView.findViewById(R.id.offerBadgeTextView);
                favoriteButton = itemView.findViewById(R.id.offerFavoriteButton);
                orderButton = itemView.findViewById(R.id.offerOrderButton);
                productImage = itemView.findViewById(R.id.offerProductImage);
            }

            public void bind(Product product) {
                try {
                    if (product == null || !isFragmentAttached || getContext() == null) return;
                    
                    if (product.getName() != null && nameTextView != null) {
                        nameTextView.setText(product.getName());
                    }
                    if (product.getCategory() != null && categoryTextView != null) {
                        categoryTextView.setText(product.getCategory());
                    }
                    if (priceTextView != null) {
                        priceTextView.setText("$" + String.format("%.2f", product.getPrice()));
                    }
                    if (stockTextView != null) {
                        stockTextView.setText("Stock: " + product.getStock());
                    }

                    // Show offer badge
                    if (offerBadgeTextView != null) {
                        offerBadgeTextView.setVisibility(View.VISIBLE);
                    }

                    // Load image using Glide with better error handling
                    if (productImage != null) {
                        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                            Glide.with(requireContext())
                                    .load(product.getImageUrl())
                                    .placeholder(R.drawable.grocery_logo)
                                    .error(R.drawable.grocery_logo)
                                    .timeout(10000) // 10 second timeout
                                    .into((android.widget.ImageView) productImage);
                        } else {
                            // Set default image if no URL
                            ((android.widget.ImageView) productImage).setImageResource(R.drawable.grocery_logo);
                        }
                    }

                    // Check if product is in favorites
                    boolean isFavorite = false;
                    try {
                        if (databaseHelper != null) {
                            isFavorite = databaseHelper.isFavorite(userEmail, product.getIdAsInt());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking favorite status: " + e.getMessage(), e);
                    }
                    updateFavoriteButton(isFavorite);

                    // Favorite button click with heart beat animation
                    if (favoriteButton != null) {
                        boolean finalIsFavorite = isFavorite;
                        favoriteButton.setOnClickListener(v -> {
                            try {
                                if (!isFragmentAttached || getContext() == null) return;
                                
                                if (finalIsFavorite) {
                                    if (databaseHelper != null) {
                                        databaseHelper.removeFromFavorites(userEmail, product.getIdAsInt());
                                    }
                                    updateFavoriteButton(false);
                                    Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (databaseHelper != null) {
                                        databaseHelper.addToFavorites(userEmail, product.getIdAsInt(), product.getName(), product.getPrice());
                                    }
                                    updateFavoriteButton(true);
                                    Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                                    
                                    // Heart beat animation
                                    Animation heartBeat = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
                                    heartBeat.setDuration(300);
                                    favoriteButton.startAnimation(heartBeat);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in favorite button click: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Error updating favorites", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // Order button click with bounce animation
                    if (orderButton != null) {
                        orderButton.setOnClickListener(v -> {
                            try {
                                if (!isFragmentAttached || getContext() == null) return;
                                
                                // Bounce animation for order button
                                Animation bounce = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
                                bounce.setDuration(200);
                                orderButton.startAnimation(bounce);
                                
                                showOrderDialog(product);
                            } catch (Exception e) {
                                Log.e(TAG, "Error in order button click: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Error opening order dialog", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error binding offer data: " + e.getMessage(), e);
                }
            }

            private void updateFavoriteButton(boolean isFavorite) {
                try {
                    if (favoriteButton != null) {
                        favoriteButton.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating favorite button: " + e.getMessage(), e);
                }
            }

            private void showOrderDialog(Product product) {
                try {
                    if (!isFragmentAttached || getContext() == null) return;

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_order, null);
                    builder.setView(dialogView);

                    EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
                    AutoCompleteTextView deliverySpinner = dialogView.findViewById(R.id.deliverySpinner);
                    Button confirmButton = dialogView.findViewById(R.id.confirmOrderButton);

                    // Setup delivery spinner
                    String[] deliveryMethods = {"Home Delivery", "Pickup" };
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

                            if (quantity > product.getStock()) {
                                quantityEditText.setError("Quantity exceeds available stock");
                                return;
                            }

                            String deliveryMethod = deliverySpinner.getText().toString();
                            if (deliveryMethod.isEmpty()) {
                                deliveryMethod = "Home Delivery";
                            }
                            
                            // Save order to database
                            if (databaseHelper != null) {
                                long orderId = databaseHelper.addOrder(userEmail, product.getIdAsInt(), product.getName(), quantity, deliveryMethod);
                                if (orderId > 0) {
                                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to place order", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(requireContext(), "Database error", Toast.LENGTH_SHORT).show();
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