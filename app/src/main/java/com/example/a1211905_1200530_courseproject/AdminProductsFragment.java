package com.example.a1211905_1200530_courseproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsFragment extends Fragment {
    private ListView productListView;
    private Button addProductButton;
    private DatabaseHelper databaseHelper;
    private List<Product> products;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);

        productListView = view.findViewById(R.id.productListView);
        addProductButton = view.findViewById(R.id.addProductButton);
        databaseHelper = new DatabaseHelper(getContext());

        loadProducts();

        addProductButton.setOnClickListener(v -> showAddProductDialog());

        return view;
    }

    private void loadProducts() {
        products = databaseHelper.getLocalProducts();
        List<String> productStrings = new ArrayList<>();

        for (Product product : products) {
            String productInfo = String.format("%s\nCategory: %s\nPrice: $%.2f\nStock: %d\nOffer: %s",
                product.getName(), product.getCategory(), product.getPrice(),
                product.getStock(), product.isOffer() ? "Yes" : "No");
            productStrings.add(productInfo);
        }

        if (adapter == null) {
            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, productStrings);
            productListView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(productStrings);
            adapter.notifyDataSetChanged();
        }

        // Show product count
        if (getActivity() != null) {
            getActivity().setTitle("Manage Products (" + products.size() + ")");
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null);

        EditText nameEdit = dialogView.findViewById(R.id.productNameEdit);
        EditText categoryEdit = dialogView.findViewById(R.id.productCategoryEdit);
        EditText priceEdit = dialogView.findViewById(R.id.productPriceEdit);
        EditText stockEdit = dialogView.findViewById(R.id.productStockEdit);
        EditText imageUrlEdit = dialogView.findViewById(R.id.productImageUrlEdit);

        builder.setView(dialogView)
                .setTitle("Add New Product")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameEdit.getText().toString();
                    String category = categoryEdit.getText().toString();
                    String priceStr = priceEdit.getText().toString();
                    String stockStr = stockEdit.getText().toString();
                    String imageUrl = imageUrlEdit.getText().toString();

                    if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);
                        int stock = Integer.parseInt(stockStr);

                        // Create new product with unique ID
                        int newId = products.size() + 1;
                        Product newProduct = new Product(String.valueOf(newId), category, name, price, stock, imageUrl, false);

                        // Add to database
                        if (databaseHelper.addProduct(newProduct)) {
                            Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                            loadProducts(); // Refresh list

                            // Also refresh the main products fragment if it's visible
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    // This will trigger the onResume in ProductsFragment
                                    if (getActivity() instanceof MainActivity) {
                                        MainActivity mainActivity = (MainActivity) getActivity();
                                        // Force refresh of products fragment
                                        mainActivity.refreshProductsFragment();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid price or stock number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts(); // Refresh when returning to fragment
    }
}
