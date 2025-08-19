package com.example.a1211905_1200530_courseproject;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductsFragment extends Fragment {
    private static final String TAG = "ProductsFragment";
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private EditText searchEditText;
    private AutoCompleteTextView categorySpinner, priceRangeSpinner;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private String userEmail;
    private boolean isFragmentAttached = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_products, container, false);

            // Initialize views
            recyclerView = view.findViewById(R.id.productsRecyclerView);
            searchEditText = view.findViewById(R.id.searchEditText);
            categorySpinner = view.findViewById(R.id.categorySpinner);
            priceRangeSpinner = view.findViewById(R.id.priceRangeSpinner);

            Log.d(TAG, "Views initialized - RecyclerView: " + (recyclerView != null));

            // Initialize database and preferences
            databaseHelper = new DatabaseHelper(requireContext());
            sharedPreferences = requireActivity().getSharedPreferences("GroceryApp", 0);
            userEmail = sharedPreferences.getString("user_email", "");
            
            Log.d(TAG, "User email retrieved: " + (userEmail != null ? userEmail : "null"));
            Log.d(TAG, "Database helper initialized: " + (databaseHelper != null));
            
            // Check database status
            if (databaseHelper != null) {
                databaseHelper.checkDatabaseStatus();
            }
            
            // Check if user exists in database
            if (userEmail != null && !userEmail.isEmpty()) {
                boolean userExists = databaseHelper.isEmailExists(userEmail);
                Log.d(TAG, "User exists in database: " + userExists);
                if (!userExists) {
                    Log.e(TAG, "User not found in database for email: " + userEmail);
                    Toast.makeText(requireContext(), "User not found. Please login again.", Toast.LENGTH_LONG).show();
                } else {
                    int userId = databaseHelper.getUserIdByEmail(userEmail);
                    Log.d(TAG, "User found in database. User ID: " + userId);
                }
            } else {
                Log.e(TAG, "User email is null or empty");
                Toast.makeText(requireContext(), "Please login to continue.", Toast.LENGTH_LONG).show();
                
                // Create a test user if no user is logged in (for testing purposes)
                if (databaseHelper != null) {
                    long testUserId = databaseHelper.addUser("test@test.com", "Test", "User", "password123", "Male", "Test City", "1234567890");
                    if (testUserId > 0) {
                        Log.d(TAG, "Created test user with ID: " + testUserId);
                        userEmail = "test@test.com";
                        Toast.makeText(requireContext(), "Created test user: test@test.com", Toast.LENGTH_LONG).show();
                    }
                }
            }

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            allProducts = new ArrayList<>();
            filteredProducts = new ArrayList<>();
            adapter = new ProductAdapter(filteredProducts);
            recyclerView.setAdapter(adapter);

            Log.d(TAG, "RecyclerView setup complete - Adapter: " + (adapter != null));

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing Products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Return a simple view to prevent crash
            return new View(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentAttached = true;
        
        Log.d(TAG, "Fragment attached, setting up spinners and loading products...");
        
        // Setup spinners
        setupSpinners();

        // Setup search functionality
        setupSearch();

                    // Load products from database first, then API if needed
            Log.d(TAG, "Loading products from database...");
            loadProducts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentAttached = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh products from database when returning to fragment
        if (isFragmentAttached && getContext() != null) {
            refreshProductsFromDatabase();
        }
    }

    private void refreshProductsFromDatabase() {
        try {
            if (!isFragmentAttached || getContext() == null) return;
            
            Log.d(TAG, "Refreshing products from database...");
            List<Product> localProducts = databaseHelper.getLocalProducts();
            if (localProducts != null) {
                allProducts = localProducts;
                filteredProducts.clear();
                filteredProducts.addAll(allProducts);
                
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Products refreshed from database: " + allProducts.size());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing products: " + e.getMessage(), e);
        }
    }

    private void setupSpinners() {
        try {
            Log.d(TAG, "setupSpinners called - isFragmentAttached: " + isFragmentAttached + ", getContext: " + (getContext() != null));
            
            if (!isFragmentAttached || getContext() == null) {
                Log.e(TAG, "Cannot setup spinners - fragment not attached or context null");
                return;
            }

            Log.d(TAG, "Setting up spinners...");

            // Category spinner
            String[] categories = {"All Categories", "Fruits", "Vegetables", "Dairy", "Bakery"};
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
            categorySpinner.setAdapter(categoryAdapter);
            categorySpinner.setText("All Categories", false);
            Log.d(TAG, "Category spinner setup complete");

            // Price range spinner
            String[] priceRanges = {"All Prices", "Under $2", "$2 - $5", "Over $5"};
            ArrayAdapter<String> priceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, priceRanges);
            priceRangeSpinner.setAdapter(priceAdapter);
            priceRangeSpinner.setText("All Prices", false);
            Log.d(TAG, "Price spinner setup complete");

            Log.d(TAG, "Spinners setup complete");

            // Add listeners for filtering
            categorySpinner.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    Log.d(TAG, "Category filter changed to: " + categories[position]);
                    if (isFragmentAttached) {
                        filterProducts();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in category filter: " + e.getMessage(), e);
                }
            });

            priceRangeSpinner.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    Log.d(TAG, "Price filter changed to: " + priceRanges[position]);
                    if (isFragmentAttached) {
                        filterProducts();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in price filter: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupSpinners: " + e.getMessage(), e);
        }
    }

    private void setupSearch() {
        try {
            if (!isFragmentAttached || searchEditText == null) return;

            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        if (isFragmentAttached) {
                            filterProducts();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in search filter: " + e.getMessage(), e);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupSearch: " + e.getMessage(), e);
        }
    }

    private void loadProducts() {
        try {
            if (!isFragmentAttached || getContext() == null) return;

            Log.d(TAG, "Starting to load products...");

            // First, try to load from local database
            List<Product> localProducts = databaseHelper.getLocalProducts();
            if (localProducts != null && !localProducts.isEmpty()) {
                Log.d(TAG, "Products loaded from local database: " + localProducts.size());
                allProducts = localProducts;
                filteredProducts.clear();
                filteredProducts.addAll(allProducts);
                
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter updated with local products");
                }
                
                // Only load from API if we don't have local products (first time)
                if (localProducts.size() < 5) { // If we have very few products, load from API
                    loadProductsFromAPI();
                }
                return;
            }

            // No local products, load from API
            loadProductsFromAPI();
        } catch (Exception e) {
            Log.e(TAG, "Error loading products: " + e.getMessage(), e);
            loadProductsFromAPI();
        }
    }

    private void loadProductsFromAPI() {
        try {
            if (!isFragmentAttached || getContext() == null) return;

            Log.d(TAG, "Loading products from API...");

            // Initialize Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://68924ed4447ff4f11fbfdeb4.mockapi.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<Product[]> call = apiService.getProducts();

            Log.d(TAG, "Making API call to load products...");

            call.enqueue(new Callback<Product[]>() {
                @Override
                public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                    try {
                        Log.d(TAG, "API Response received - Success: " + response.isSuccessful() + ", Code: " + response.code());
                        
                        if (!isFragmentAttached || getContext() == null) {
                            Log.d(TAG, "Fragment not attached, skipping response");
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            allProducts = Arrays.asList(response.body());
                            Log.d(TAG, "Products loaded from API: " + allProducts.size());
                            
                            // Save products to local database
                            if (databaseHelper != null) {
                                databaseHelper.saveProducts(allProducts);
                            }
                            
                            if (isFragmentAttached && getContext() != null) {
                                Toast.makeText(requireContext(), "Loaded " + allProducts.size() + " products from API", Toast.LENGTH_SHORT).show();
                            }
                            
                            // Clear filtered products and add all API products
                            filteredProducts.clear();
                            filteredProducts.addAll(allProducts);
                            
                            Log.d(TAG, "Filtered products after API load: " + filteredProducts.size());
                            
                            // Force adapter update
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "Adapter notified of data change after API load");
                            }
                        } else {
                            Log.e(TAG, "Failed to load products: " + response.message() + ", Code: " + response.code());
                            if (isFragmentAttached && getContext() != null) {
                                Toast.makeText(requireContext(), "Failed to load products: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                            loadLocalProducts();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing products: " + e.getMessage(), e);
                        if (isFragmentAttached && getContext() != null) {
                            Toast.makeText(requireContext(), "Error processing products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        loadDummyProducts();
                    }
                }

                @Override
                public void onFailure(Call<Product[]> call, Throwable t) {
                    Log.e(TAG, "Network error: " + t.getMessage(), t);
                    if (isFragmentAttached && getContext() != null) {
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    loadDummyProducts();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up API: " + e.getMessage(), e);
            if (isFragmentAttached && getContext() != null) {
                Toast.makeText(requireContext(), "Error setting up API: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            loadDummyProducts();
        }
    }

    private void loadLocalProducts() {
        try {
            Log.d(TAG, "Loading local products...");
            
            if (!isFragmentAttached) {
                Log.d(TAG, "Fragment not attached, skipping local products");
                return;
            }

            // Load products from local database
            allProducts = databaseHelper.getLocalProducts();
            Log.d(TAG, "Products loaded from local database: " + allProducts.size());
            
            if (allProducts.isEmpty()) {
                Log.d(TAG, "No local products found, loading dummy products");
                loadDummyProducts();
                return;
            }
            
            if (isFragmentAttached && getContext() != null) {
                Toast.makeText(requireContext(), "Loaded " + allProducts.size() + " local products", Toast.LENGTH_LONG).show();
            }
            
            // Clear filtered products and add all local products
            filteredProducts.clear();
            filteredProducts.addAll(allProducts);
            
            Log.d(TAG, "Filtered products after local load: " + filteredProducts.size());
            
            // Force adapter update
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Adapter notified of data change after local load");
            } else {
                Log.e(TAG, "Adapter is null after local load!");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading local products: " + e.getMessage(), e);
            loadDummyProducts();
        }
    }

    private void loadDummyProducts() {
        try {
            Log.d(TAG, "Loading dummy products...");
            
            if (!isFragmentAttached) {
                Log.d(TAG, "Fragment not attached, skipping dummy products");
                return;
            }

            // Create dummy products for testing when API fails
            allProducts = new ArrayList<>();
            allProducts.add(new Product("1", "Fruits", "Apple", 2.5, 50, "https://images.pexels.com/photos/588587/pexels-photo-588587.jpeg?cs=srgb&dl=apple-blur-bright-588587.jpg&fm=jpg", false));
            allProducts.add(new Product("2", "Fruits", "Banana", 1.8, 40, "https://example.com/images/banana.png", true));
            allProducts.add(new Product("3", "Vegetables", "Tomato", 2.0, 60, "https://example.com/images/tomato.png", false));
            allProducts.add(new Product("4", "Dairy", "Milk 1L", 4.0, 25, "https://example.com/images/milk.png", false));
            allProducts.add(new Product("5", "Bakery", "Bread", 3.0, 20, "https://example.com/images/bread.png", false));
            
            Log.d(TAG, "Dummy products created: " + allProducts.size());
            
            if (isFragmentAttached && getContext() != null) {
                Toast.makeText(requireContext(), "Loaded " + allProducts.size() + " dummy products", Toast.LENGTH_LONG).show();
            }
            
            // Clear filtered products and add all dummy products
            filteredProducts.clear();
            filteredProducts.addAll(allProducts);
            
            Log.d(TAG, "Filtered products after dummy load: " + filteredProducts.size());
            
            // Force adapter update
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Adapter notified of data change after dummy load");
            } else {
                Log.e(TAG, "Adapter is null after dummy load!");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading dummy products: " + e.getMessage(), e);
        }
    }

    private void filterProducts() {
        try {
            Log.d(TAG, "Filtering products...");
            
            if (!isFragmentAttached || allProducts == null || searchEditText == null || categorySpinner == null || priceRangeSpinner == null) {
                Log.d(TAG, "Cannot filter products - fragment not attached or views null");
                return;
            }
            
            String searchQuery = searchEditText.getText().toString().toLowerCase();
            String selectedCategory = categorySpinner.getText().toString();
            String selectedPriceRange = priceRangeSpinner.getText().toString();

            Log.d(TAG, "Filter criteria - Search: '" + searchQuery + "', Category: '" + selectedCategory + "', Price: '" + selectedPriceRange + "'");

            filteredProducts.clear();

            for (Product product : allProducts) {
                if (product != null && product.getName() != null && product.getCategory() != null) {
                    boolean matchesSearch = product.getName().toLowerCase().contains(searchQuery);
                    boolean matchesCategory = selectedCategory.equals("All Categories") || product.getCategory().equals(selectedCategory);
                    boolean matchesPrice = matchesPriceRange(product.getPrice(), selectedPriceRange);

                    if (matchesSearch && matchesCategory && matchesPrice) {
                        filteredProducts.add(product);
                        Log.d(TAG, "Added product to filtered list: " + product.getName());
                    }
                }
            }

            Log.d(TAG, "Filtered products: " + filteredProducts.size());

            if (adapter != null && isFragmentAttached) {
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Adapter notified of data change");
            } else {
                Log.e(TAG, "Adapter is null or fragment not attached");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering products: " + e.getMessage(), e);
        }
    }

    private boolean matchesPriceRange(double price, String range) {
        switch (range) {
            case "All Prices":
                return true;
            case "Under $2":
                return price < 2.0;
            case "$2 - $5":
                return price >= 2.0 && price <= 5.0;
            case "Over $5":
                return price > 5.0;
            default:
                return true;
        }
    }

    // API Service interface
    public interface ApiService {
        @retrofit2.http.GET("products")
        Call<Product[]> getProducts();
    }

    // Product Adapter
    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Product> products;

        public ProductAdapter(List<Product> products) {
            this.products = products;
        }

        @Override
        public int getItemCount() {
            int count = products != null ? products.size() : 0;
            Log.d(TAG, "Adapter getItemCount called: " + count);
            return count;
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            try {
                Log.d(TAG, "Binding product at position " + position);
                if (products != null && position < products.size()) {
                    Product product = products.get(position);
                    Log.d(TAG, "Binding product: " + product.getName());
                    holder.bind(product);
                } else {
                    Log.e(TAG, "Cannot bind product - products null or position out of bounds");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error binding product at position " + position + ": " + e.getMessage(), e);
            }
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "Creating ViewHolder");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTextView, categoryTextView, priceTextView, stockTextView;
            private ImageButton favoriteButton;
            private Button orderButton;
            private View productImage;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                try {
                    nameTextView = itemView.findViewById(R.id.productNameTextView);
                    categoryTextView = itemView.findViewById(R.id.productCategoryTextView);
                    priceTextView = itemView.findViewById(R.id.productPriceTextView);
                    stockTextView = itemView.findViewById(R.id.productStockTextView);
                    favoriteButton = itemView.findViewById(R.id.favoriteButton);
                    orderButton = itemView.findViewById(R.id.orderButton);
                    productImage = itemView.findViewById(R.id.productImage);
                } catch (Exception e) {
                    Log.e(TAG, "Error finding views in ProductViewHolder: " + e.getMessage(), e);
                }
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
                            int productIdInt = product.getIdAsInt();
                            Log.d(TAG, "Product ID conversion - Original: " + product.getId() + ", Converted: " + productIdInt);
                            isFavorite = databaseHelper.isFavorite(userEmail, productIdInt);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking favorite status: " + e.getMessage(), e);
                    }
                    updateFavoriteButton(isFavorite);

                    // Favorite button click with heart beat animation
                    if (favoriteButton != null) {
                        favoriteButton.setOnClickListener(v -> {
                            try {
                                if (!isFragmentAttached || getContext() == null) return;
                                
                                // Check current favorite status dynamically
                                boolean currentFavoriteStatus = false;
                                if (databaseHelper != null) {
                                    int productIdInt = product.getIdAsInt();
                                    Log.d(TAG, "Favorite button click - Product ID: " + product.getId() + ", Converted: " + productIdInt);
                                    currentFavoriteStatus = databaseHelper.isFavorite(userEmail, productIdInt);
                                    Log.d(TAG, "Current favorite status for product " + product.getId() + ": " + currentFavoriteStatus);
                                }
                                
                                if (currentFavoriteStatus) {
                                    // Remove from favorites
                                    if (databaseHelper != null) {
                                        int productIdInt = product.getIdAsInt();
                                        boolean success = databaseHelper.removeFromFavorites(userEmail, productIdInt);
                                        Log.d(TAG, "Remove from favorites result: " + success + " for product " + product.getId());
                                        if (success) {
                                            updateFavoriteButton(false);
                                            Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                                            
                                            // Heart beat animation
                                            Animation heartBeat = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
                                            heartBeat.setDuration(300);
                                            favoriteButton.startAnimation(heartBeat);
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    // Add to favorites
                                    if (databaseHelper != null) {
                                        int productIdInt = product.getIdAsInt();
                                        long favoriteId = databaseHelper.addToFavorites(userEmail, productIdInt, product.getName(), product.getPrice());
                                        Log.d(TAG, "Add to favorites result: " + favoriteId + " for product " + product.getId());
                                        if (favoriteId > 0) {
                                            updateFavoriteButton(true);
                                            Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                                            
                                            // Heart beat animation
                                            Animation heartBeat = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
                                            heartBeat.setDuration(300);
                                            favoriteButton.startAnimation(heartBeat);
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                                        }
                                    }
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
                    Log.e(TAG, "Error binding product: " + e.getMessage(), e);
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
                                if (userEmail == null || userEmail.isEmpty()) {
                                    Toast.makeText(requireContext(), "User not logged in. Please login again.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                
                                Log.d(TAG, "Attempting to place order - User: " + userEmail + ", Product: " + product.getId() + ", Quantity: " + quantity);
                                int productIdInt = product.getIdAsInt();
                                Log.d(TAG, "Order - Product ID conversion: " + product.getId() + " -> " + productIdInt);
                                long orderId = databaseHelper.addOrder(userEmail, productIdInt, product.getName(), quantity, deliveryMethod);
                                Log.d(TAG, "Order result: " + orderId + " for product " + product.getId());
                                if (orderId > 0) {
                                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Log.e(TAG, "Failed to place order - orderId: " + orderId);
                                    Toast.makeText(requireContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
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