package com.example.a1211905_1200530_courseproject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GroceryDB";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_PRODUCTS = "products";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_EMAIL = "email";

    // Users table columns
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_CITY = "city";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PROFILE_PICTURE = "profile_picture";

    // Orders table columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PRODUCT_NAME = "product_name";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_PRICE = "price";
    private static final String KEY_DELIVERY_METHOD = "delivery_method";
    private static final String KEY_ORDER_TIME = "order_time";
    private static final String KEY_STATUS = "status";

    // Favorites table columns
    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_PRODUCT_IMAGE = "product_image";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_STOCK = "stock";

    // Products table columns
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_OFFER = "offer";

    // Create table statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_EMAIL + " TEXT UNIQUE,"
            + KEY_FIRST_NAME + " TEXT,"
            + KEY_LAST_NAME + " TEXT,"
            + KEY_PASSWORD + " TEXT,"
            + KEY_GENDER + " TEXT,"
            + KEY_CITY + " TEXT,"
            + KEY_PHONE + " TEXT,"
            + KEY_PROFILE_PICTURE + " TEXT"
            + ")";

    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE " + TABLE_ORDERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_PRODUCT_NAME + " TEXT,"
            + KEY_QUANTITY + " INTEGER,"
            + KEY_PRICE + " REAL,"
            + KEY_DELIVERY_METHOD + " TEXT,"
            + KEY_ORDER_TIME + " TEXT,"
            + KEY_STATUS + " TEXT,"
            + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
            + ")";

    private static final String CREATE_TABLE_FAVORITES = "CREATE TABLE " + TABLE_FAVORITES + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_PRODUCT_ID + " TEXT,"
            + KEY_PRODUCT_NAME + " TEXT,"
            + KEY_PRODUCT_IMAGE + " TEXT,"
            + KEY_CATEGORY + " TEXT,"
            + KEY_PRICE + " REAL,"
            + KEY_STOCK + " INTEGER,"
            + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
            + ")";

    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + TABLE_PRODUCTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_CATEGORY + " TEXT,"
            + KEY_PRICE + " REAL,"
            + KEY_STOCK + " INTEGER,"
            + KEY_IMAGE_URL + " TEXT,"
            + KEY_OFFER + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating database tables...");
        db.execSQL(CREATE_TABLE_USERS);
        Log.d("DatabaseHelper", "Users table created");
        db.execSQL(CREATE_TABLE_ORDERS);
        Log.d("DatabaseHelper", "Orders table created");
        db.execSQL(CREATE_TABLE_FAVORITES);
        Log.d("DatabaseHelper", "Favorites table created");
        db.execSQL(CREATE_TABLE_PRODUCTS);
        Log.d("DatabaseHelper", "Products table created");
        Log.d("DatabaseHelper", "All tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    // User operations
    public long addUser(String email, String firstName, String lastName, String password, 
                       String gender, String city, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_EMAIL, email);
        values.put(KEY_FIRST_NAME, firstName);
        values.put(KEY_LAST_NAME, lastName);
        values.put(KEY_PASSWORD, encryptPassword(password));
        values.put(KEY_GENDER, gender);
        values.put(KEY_CITY, city);
        values.put(KEY_PHONE, phone);
        
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_ID};
        String selection = KEY_EMAIL + "=? AND " + KEY_PASSWORD + "=?";
        String[] selectionArgs = {email, encryptPassword(password)};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        
        return count > 0;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_ID};
        String selection = KEY_EMAIL + "=?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        
        return count > 0;
    }

    @SuppressLint("Range")
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_EMAIL + "=?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
            user.setFirstName(cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)));
            user.setLastName(cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME)));
            user.setGender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));
            user.setCity(cursor.getString(cursor.getColumnIndex(KEY_CITY)));
            user.setPhone(cursor.getString(cursor.getColumnIndex(KEY_PHONE)));
            user.setProfilePicture(cursor.getString(cursor.getColumnIndex(KEY_PROFILE_PICTURE)));
        }
        
        cursor.close();
        db.close();
        return user;
    }

    public boolean updateUserProfile(int userId, String firstName, String lastName, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_FIRST_NAME, firstName);
        values.put(KEY_LAST_NAME, lastName);
        values.put(KEY_PHONE, phone);
        
        String whereClause = KEY_ID + "=?";
        String[] whereArgs = {String.valueOf(userId)};
        
        int result = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_PASSWORD, encryptPassword(newPassword));
        
        String whereClause = KEY_ID + "=?";
        String[] whereArgs = {String.valueOf(userId)};
        
        int result = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    // Order operations
    public long addOrder(int userId, String productName, int quantity, double price, 
                        String deliveryMethod) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_USER_ID, userId);
        values.put(KEY_PRODUCT_NAME, productName);
        values.put(KEY_QUANTITY, quantity);
        values.put(KEY_PRICE, price);
        values.put(KEY_DELIVERY_METHOD, deliveryMethod);
        values.put(KEY_ORDER_TIME, System.currentTimeMillis());
        values.put(KEY_STATUS, "Pending");
        
        long id = db.insert(TABLE_ORDERS, null, values);
        db.close();
        return id;
    }

    @SuppressLint("Range")
    public List<Order> getUserOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = KEY_ORDER_TIME + " DESC";
        
        Cursor cursor = db.query(TABLE_ORDERS, null, selection, selectionArgs, null, null, orderBy);
        
        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                order.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                order.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                order.setQuantity(cursor.getInt(cursor.getColumnIndex(KEY_QUANTITY)));
                order.setPrice(cursor.getDouble(cursor.getColumnIndex(KEY_PRICE)));
                order.setDeliveryMethod(cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_METHOD)));
                order.setOrderTime(cursor.getLong(cursor.getColumnIndex(KEY_ORDER_TIME)));
                order.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
                orders.add(order);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return orders;
    }

    // Favorite operations
    public long addToFavorites(int userId, String productId, String productName, 
                              String productImage, String category, double price, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_USER_ID, userId);
        values.put(KEY_PRODUCT_ID, productId);
        values.put(KEY_PRODUCT_NAME, productName);
        values.put(KEY_PRODUCT_IMAGE, productImage);
        values.put(KEY_CATEGORY, category);
        values.put(KEY_PRICE, price);
        values.put(KEY_STOCK, stock);
        
        long id = db.insert(TABLE_FAVORITES, null, values);
        db.close();
        return id;
    }

    public boolean removeFromFavorites(int userId, String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_USER_ID + "=? AND " + KEY_PRODUCT_ID + "=?";
        String[] whereArgs = {String.valueOf(userId), productId};
        
        int result = db.delete(TABLE_FAVORITES, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    public boolean isFavorite(int userId, String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_ID};
        String selection = KEY_USER_ID + "=? AND " + KEY_PRODUCT_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId), productId};
        
        Cursor cursor = db.query(TABLE_FAVORITES, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        
        return count > 0;
    }

    @SuppressLint("Range")
    public List<Favorite> getUserFavorites(int userId) {
        List<Favorite> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = KEY_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                Favorite favorite = new Favorite();
                favorite.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                favorite.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                favorite.setProductId(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_ID)));
                favorite.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                favorite.setProductImage(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_IMAGE)));
                favorite.setCategory(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)));
                favorite.setPrice(cursor.getDouble(cursor.getColumnIndex(KEY_PRICE)));
                favorite.setStock(cursor.getInt(cursor.getColumnIndex(KEY_STOCK)));
                favorites.add(favorite);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return favorites;
    }

    // Admin operations
    @SuppressLint("Range")
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
                user.setFirstName(cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)));
                user.setLastName(cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME)));
                user.setGender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));
                user.setCity(cursor.getString(cursor.getColumnIndex(KEY_CITY)));
                user.setPhone(cursor.getString(cursor.getColumnIndex(KEY_PHONE)));
                users.add(user);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return users;
    }

    // Get all orders for admin
    @SuppressLint("Range")
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, KEY_ORDER_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                order.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                order.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                order.setQuantity(cursor.getInt(cursor.getColumnIndex(KEY_QUANTITY)));
                
                // Handle price field - it might be null
                int priceColumnIndex = cursor.getColumnIndex(KEY_PRICE);
                if (!cursor.isNull(priceColumnIndex)) {
                    order.setPrice(cursor.getDouble(priceColumnIndex));
                } else {
                    order.setPrice(0.0);
                }
                
                order.setDeliveryMethod(cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_METHOD)));
                
                // Handle order_time as TEXT field
                String orderTimeStr = cursor.getString(cursor.getColumnIndex(KEY_ORDER_TIME));
                if (orderTimeStr != null && !orderTimeStr.isEmpty()) {
                    try {
                        order.setOrderTime(Long.parseLong(orderTimeStr));
                    } catch (NumberFormatException e) {
                        order.setOrderTime(System.currentTimeMillis());
                    }
                } else {
                    order.setOrderTime(System.currentTimeMillis());
                }
                
                order.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
                orders.add(order);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return orders;
    }

    // Update order status
    public boolean updateOrderStatus(int orderId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, newStatus);
        
        String whereClause = KEY_ID + "=?";
        String[] whereArgs = {String.valueOf(orderId)};
        
        int result = db.update(TABLE_ORDERS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    // Add new product
    public boolean addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_ID, product.getIdAsInt());
        values.put(KEY_NAME, product.getName());
        values.put(KEY_CATEGORY, product.getCategory());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_STOCK, product.getStock());
        values.put(KEY_IMAGE_URL, product.getImageUrl());
        values.put(KEY_OFFER, product.isOffer() ? 1 : 0);
        
        long result = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        
        return result != -1;
    }

    // Delete product
    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_ID + "=?";
        String[] whereArgs = {String.valueOf(productId)};
        
        int result = db.delete(TABLE_PRODUCTS, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    // Update product
    public boolean updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_NAME, product.getName());
        values.put(KEY_CATEGORY, product.getCategory());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_STOCK, product.getStock());
        values.put(KEY_IMAGE_URL, product.getImageUrl());
        values.put(KEY_OFFER, product.isOffer() ? 1 : 0);
        
        String whereClause = KEY_ID + "=?";
        String[] whereArgs = {String.valueOf(product.getIdAsInt())};
        
        int result = db.update(TABLE_PRODUCTS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = true;
        
        try {
            db.beginTransaction();
            
            // Delete user's orders
            String ordersWhereClause = KEY_USER_ID + "=?";
            String[] ordersWhereArgs = {String.valueOf(userId)};
            db.delete(TABLE_ORDERS, ordersWhereClause, ordersWhereArgs);
            
            // Delete user's favorites
            String favoritesWhereClause = KEY_USER_ID + "=?";
            String[] favoritesWhereArgs = {String.valueOf(userId)};
            db.delete(TABLE_FAVORITES, favoritesWhereClause, favoritesWhereArgs);
            
            // Finally delete the user
            String userWhereClause = KEY_ID + "=?";
            String[] userWhereArgs = {String.valueOf(userId)};
            int result = db.delete(TABLE_USERS, userWhereClause, userWhereArgs);
            
            if (result > 0) {
                db.setTransactionSuccessful();
            } else {
                success = false;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting user: " + e.getMessage(), e);
            success = false;
        } finally {
            db.endTransaction();
            db.close();
        }
        
        return success;
    }

    // Profile update methods using email
    public boolean updateUserProfile(String email, String firstName, String lastName, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_FIRST_NAME, firstName);
        values.put(KEY_LAST_NAME, lastName);
        values.put(KEY_PHONE, phone);
        
        String whereClause = KEY_EMAIL + "=?";
        String[] whereArgs = {email};
        
        int result = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    public boolean updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String encryptedPassword = encryptPassword(newPassword);
        values.put(KEY_PASSWORD, encryptedPassword);
        
        String whereClause = KEY_EMAIL + "=?";
        String[] whereArgs = {email};
        
        int result = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    // Order methods using email instead of userId
    public long addOrder(String userEmail, int productId, String productName, int quantity, String deliveryMethod) {
        SQLiteDatabase db = null;
        long id = -1;
        
        try {
            Log.d("DatabaseHelper", "addOrder called - userEmail: " + userEmail + ", productId: " + productId + ", productName: " + productName);
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            
            int userId = getUserIdByEmail(userEmail, db);
            Log.d("DatabaseHelper", "getUserIdByEmail result: " + userId + " for email: " + userEmail);
            if (userId == -1) {
                Log.e("DatabaseHelper", "User not found for email: " + userEmail);
                return -1;
            }
            
            values.put(KEY_USER_ID, userId);
            values.put(KEY_PRODUCT_NAME, productName);
            values.put(KEY_QUANTITY, quantity);
            values.put(KEY_DELIVERY_METHOD, deliveryMethod);
            values.put(KEY_ORDER_TIME, String.valueOf(System.currentTimeMillis())); // Convert to string for TEXT field
            values.put(KEY_STATUS, "Pending");
            
            Log.d("DatabaseHelper", "Inserting order with values: " + values.toString());
            id = db.insert(TABLE_ORDERS, null, values);
            Log.d("DatabaseHelper", "Order added with ID: " + id + " for user: " + userEmail);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding order: " + e.getMessage(), e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return id;
    }

    @SuppressLint("Range")
    public List<Order> getUserOrders(String userEmail) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            String selection = KEY_USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(getUserIdByEmail(userEmail, db))};
            String orderBy = KEY_ORDER_TIME + " DESC";
            
            cursor = db.query(TABLE_ORDERS, null, selection, selectionArgs, null, null, orderBy);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = new Order();
                    order.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                    order.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                    order.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                    order.setQuantity(cursor.getInt(cursor.getColumnIndex(KEY_QUANTITY)));
                    
                    // Handle price field - it might be null
                    int priceColumnIndex = cursor.getColumnIndex(KEY_PRICE);
                    if (!cursor.isNull(priceColumnIndex)) {
                        order.setPrice(cursor.getDouble(priceColumnIndex));
                    } else {
                        order.setPrice(0.0);
                    }
                    
                    order.setDeliveryMethod(cursor.getString(cursor.getColumnIndex(KEY_DELIVERY_METHOD)));
                    
                    // Handle order_time as TEXT field
                    String orderTimeStr = cursor.getString(cursor.getColumnIndex(KEY_ORDER_TIME));
                    if (orderTimeStr != null && !orderTimeStr.isEmpty()) {
                        try {
                            order.setOrderTime(Long.parseLong(orderTimeStr));
                        } catch (NumberFormatException e) {
                            order.setOrderTime(System.currentTimeMillis());
                        }
                    } else {
                        order.setOrderTime(System.currentTimeMillis());
                    }
                    
                    order.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
                    orders.add(order);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user orders: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        
        return orders;
    }

    // Favorite methods using email instead of userId
    public long addToFavorites(String userEmail, int productId, String productName, double price) {
        SQLiteDatabase db = null;
        long id = -1;
        
        try {
            Log.d("DatabaseHelper", "addToFavorites called - userEmail: " + userEmail + ", productId: " + productId + ", productName: " + productName);
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            
            int userId = getUserIdByEmail(userEmail, db);
            Log.d("DatabaseHelper", "getUserIdByEmail result: " + userId + " for email: " + userEmail);
            if (userId == -1) {
                Log.e("DatabaseHelper", "User not found for email: " + userEmail);
                return -1;
            }
            
            values.put(KEY_USER_ID, userId);
            values.put(KEY_PRODUCT_ID, String.valueOf(productId));
            values.put(KEY_PRODUCT_NAME, productName);
            values.put(KEY_PRICE, price);
            
            Log.d("DatabaseHelper", "Inserting favorite with values: " + values.toString());
            id = db.insert(TABLE_FAVORITES, null, values);
            Log.d("DatabaseHelper", "Favorite added with ID: " + id + " for user: " + userEmail + ", product: " + productId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding to favorites: " + e.getMessage(), e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return id;
    }

    public boolean removeFromFavorites(String userEmail, int productId) {
        SQLiteDatabase db = null;
        boolean result = false;
        
        try {
            db = this.getWritableDatabase();
            int userId = getUserIdByEmail(userEmail, db);
            if (userId == -1) {
                Log.e("DatabaseHelper", "User not found for email: " + userEmail);
                return false;
            }
            
            String whereClause = KEY_USER_ID + "=? AND " + KEY_PRODUCT_ID + "=?";
            String[] whereArgs = {String.valueOf(userId), String.valueOf(productId)};
            
            int deleteResult = db.delete(TABLE_FAVORITES, whereClause, whereArgs);
            result = deleteResult > 0;
            Log.d("DatabaseHelper", "Favorite removed: " + result + " for user: " + userEmail + ", product: " + productId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error removing from favorites: " + e.getMessage(), e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        return result;
    }

    public boolean isFavorite(String userEmail, int productId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean result = false;
        
        try {
            db = this.getReadableDatabase();
            int userId = getUserIdByEmail(userEmail, db);
            if (userId == -1) {
                Log.e("DatabaseHelper", "User not found for email: " + userEmail);
                return false;
            }
            
            String[] columns = {KEY_ID};
            String selection = KEY_USER_ID + "=? AND " + KEY_PRODUCT_ID + "=?";
            String[] selectionArgs = {String.valueOf(userId), String.valueOf(productId)};
            
            cursor = db.query(TABLE_FAVORITES, columns, selection, selectionArgs, null, null, null);
            result = cursor != null && cursor.getCount() > 0;
            Log.d("DatabaseHelper", "Favorite check: " + result + " for user: " + userEmail + ", product: " + productId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking favorite status: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        
        return result;
    }

    @SuppressLint("Range")
    public List<Favorite> getUserFavorites(String userEmail) {
        List<Favorite> favorites = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            String selection = KEY_USER_ID + "=?";
            String[] selectionArgs = {String.valueOf(getUserIdByEmail(userEmail, db))};
            
            cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Favorite favorite = new Favorite();
                    favorite.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                    favorite.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                    favorite.setProductId(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_ID)));
                    favorite.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                    favorite.setPrice(cursor.getDouble(cursor.getColumnIndex(KEY_PRICE)));
                    favorites.add(favorite);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user favorites: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        
        return favorites;
    }

    // Products methods
    public void saveProducts(List<Product> products) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Clear existing products
            db.delete(TABLE_PRODUCTS, null, null);
            
            // Insert new products
            for (Product product : products) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, product.getIdAsInt()); // Convert string ID to int
                values.put(KEY_NAME, product.getName());
                values.put(KEY_CATEGORY, product.getCategory());
                values.put(KEY_PRICE, product.getPrice());
                values.put(KEY_STOCK, product.getStock());
                values.put(KEY_IMAGE_URL, product.getImageUrl());
                values.put(KEY_OFFER, product.isOffer() ? 1 : 0);
                
                db.insert(TABLE_PRODUCTS, null, values);
            }
            db.setTransactionSuccessful();
            Log.d("DatabaseHelper", "Saved " + products.size() + " products to local database");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error saving products: " + e.getMessage(), e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @SuppressLint("Range")
    public List<Product> getLocalProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);
            
            if (cursor.moveToFirst()) {
                do {
                    Product product = new Product(
                        String.valueOf(cursor.getInt(cursor.getColumnIndex(KEY_ID))), // Convert int to string
                        cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)),
                        cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_PRICE)),
                        cursor.getInt(cursor.getColumnIndex(KEY_STOCK)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE_URL)),
                        cursor.getInt(cursor.getColumnIndex(KEY_OFFER)) == 1
                    );
                    products.add(product);
                } while (cursor.moveToNext());
            }
            
            Log.d("DatabaseHelper", "Retrieved " + products.size() + " products from local database");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving products: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        
        return products;
    }

    public boolean hasLocalProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean hasProducts = false;
        
        try {
            cursor = db.query(TABLE_PRODUCTS, new String[]{KEY_ID}, null, null, null, null, null, "1");
            hasProducts = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking local products: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        
        return hasProducts;
    }

    // Debug method to check database status
    public void checkDatabaseStatus() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            
            // Check users table
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
            if (cursor.moveToFirst()) {
                int userCount = cursor.getInt(0);
                Log.d("DatabaseHelper", "Users table has " + userCount + " records");
            }
            cursor.close();
            
            // Check orders table
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS, null);
            if (cursor.moveToFirst()) {
                int orderCount = cursor.getInt(0);
                Log.d("DatabaseHelper", "Orders table has " + orderCount + " records");
            }
            cursor.close();
            
            // Check favorites table
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FAVORITES, null);
            if (cursor.moveToFirst()) {
                int favoriteCount = cursor.getInt(0);
                Log.d("DatabaseHelper", "Favorites table has " + favoriteCount + " records");
            }
            cursor.close();
            
            // Check products table
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
            if (cursor.moveToFirst()) {
                int productCount = cursor.getInt(0);
                Log.d("DatabaseHelper", "Products table has " + productCount + " records");
            }
            cursor.close();
            
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking database status: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    // Helper method to get userId by email
    // New overloaded method that accepts an already open database connection
    @SuppressLint("Range")
    public int getUserIdByEmail(String email, SQLiteDatabase db) {
        Cursor cursor = null;
        int userId = -1;
        
        try {
            Log.d("DatabaseHelper", "getUserIdByEmail called for email: " + email + " with provided db");
            String[] columns = {KEY_ID};
            String selection = KEY_EMAIL + "=?";
            String[] selectionArgs = {email};
            
            Log.d("DatabaseHelper", "Querying users table with selection: " + selection + ", args: " + java.util.Arrays.toString(selectionArgs));
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                Log.d("DatabaseHelper", "Found user ID: " + userId + " for email: " + email);
            } else {
                Log.e("DatabaseHelper", "No user found for email: " + email + ", cursor count: " + (cursor != null ? cursor.getCount() : "null"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user ID by email: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Don't close the database here - it's managed by the calling method
        }
        
        return userId;
    }

    @SuppressLint("Range")
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int userId = -1;
        
        try {
            Log.d("DatabaseHelper", "getUserIdByEmail called for email: " + email);
            db = this.getReadableDatabase();
            String[] columns = {KEY_ID};
            String selection = KEY_EMAIL + "=?";
            String[] selectionArgs = {email};
            
            Log.d("DatabaseHelper", "Querying users table with selection: " + selection + ", args: " + java.util.Arrays.toString(selectionArgs));
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                Log.d("DatabaseHelper", "Found user ID: " + userId + " for email: " + email);
            } else {
                Log.e("DatabaseHelper", "No user found for email: " + email + ", cursor count: " + (cursor != null ? cursor.getCount() : "null"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user ID by email: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        
        return userId;
    }

    // Password encryption (simple Caesar cipher)
    private String encryptPassword(String password) {
        StringBuilder encrypted = new StringBuilder();
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                encrypted.append((char) (((c - base + 3) % 26) + base));
            } else {
                encrypted.append(c);
            }
        }
        return encrypted.toString();
    }

    // Data classes
    public static class User {
        private int id;
        private String email, firstName, lastName, gender, city, phone, profilePicture;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getProfilePicture() { return profilePicture; }
        public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    }

    public static class Order {
        private int id, userId, quantity;
        private String productName, deliveryMethod, status;
        private double price;
        private long orderTime;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getDeliveryMethod() { return deliveryMethod; }
        public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public long getOrderTime() { return orderTime; }
        public void setOrderTime(long orderTime) { this.orderTime = orderTime; }
    }

    public static class Favorite {
        private int id, userId, stock;
        private String productId, productName, productImage, category;
        private double price;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
} 