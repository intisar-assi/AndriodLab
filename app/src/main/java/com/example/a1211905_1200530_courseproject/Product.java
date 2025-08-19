package com.example.a1211905_1200530_courseproject;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private String id; // Changed to String to handle API response
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("price")
    private double price;
    
    @SerializedName("stock")
    private int stock;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("offer")
    private boolean offer;
    
    // Constructor
    public Product(String id, String category, String name, double price, int stock, String imageUrl, boolean offer) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.offer = offer;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public int getIdAsInt() {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getName() {
        return name;
    }
    
    public double getPrice() {
        return price;
    }
    
    public int getStock() {
        return stock;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public boolean isOffer() {
        return offer;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setOffer(boolean offer) {
        this.offer = offer;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", imageUrl='" + imageUrl + '\'' +
                ", offer=" + offer +
                '}';
    }
} 