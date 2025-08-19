package com.example.a1211905_1200530_courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IntroActivity extends AppCompatActivity {
    private Button connectButton;
    private ProgressBar progressBar;
    private TextView descriptionText;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        connectButton = findViewById(R.id.connectButton);
        progressBar = findViewById(R.id.progressBar);
        descriptionText = findViewById(R.id.descriptionText);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://68924ed4447ff4f11fbfdeb4.mockapi.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCategories();
            }
        });
    }

    private void fetchCategories() {
        showLoading(true);

        Call<Product[]> call = apiService.getProducts();
        call.enqueue(new Callback<Product[]>() {
            @Override
            public void onResponse(Call<Product[]> call, Response<Product[]> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Store products in SharedPreferences or pass to next activity
                    Product[] products = response.body();

                    Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(IntroActivity.this, "Failed to fetch products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product[]> call, Throwable t) {
                showLoading(false);
                Toast.makeText(IntroActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        connectButton.setEnabled(!show);
    }

    // API Service interface
    public interface ApiService {
        @retrofit2.http.GET("products")
        Call<Product[]> getProducts();
    }
}