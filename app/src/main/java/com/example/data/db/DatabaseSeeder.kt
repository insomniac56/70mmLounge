package com.example.data.db

import com.example.data.model.KitchenOrderTicket
import com.example.data.model.ProductItem
import com.example.data.model.RestaurantTable
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem
import com.example.util.TableUrlGenerator
import java.util.Calendar

object DatabaseSeeder {

    val sampleRestaurantProducts = listOf(
        // === 1. SOUPS ===
        ProductItem(name = "Cream Of Tomato", category = "Soups", sku = "SOP-01", sellingPrice = 110.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Manchow", category = "Soups", sku = "SOP-02", sellingPrice = 140.0, stockQuantity = 45, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Sweet Corn", category = "Soups", sku = "SOP-03", sellingPrice = 140.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Hot & Sour", category = "Soups", sku = "SOP-04", sellingPrice = 160.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Manchow", category = "Soups", sku = "SOP-05", sellingPrice = 160.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Clear Soup", category = "Soups", sku = "SOP-06", sellingPrice = 120.0, stockQuantity = 30, kitchenSection = "KITCHEN"),

        // === 2. APPETIZERS & CHAAP ===
        ProductItem(name = "Paneer Pakoda", category = "Appetizers & Chaap", sku = "APP-01", sellingPrice = 200.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Onion Chhita Pakoda", category = "Appetizers & Chaap", sku = "APP-02", sellingPrice = 160.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Pakoda", category = "Appetizers & Chaap", sku = "APP-03", sellingPrice = 250.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Peanut Masala", category = "Appetizers & Chaap", sku = "APP-04", sellingPrice = 100.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Cheese Balls", category = "Appetizers & Chaap", sku = "APP-05", sellingPrice = 240.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "French Fries", category = "Appetizers & Chaap", sku = "APP-06", sellingPrice = 140.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "French Fries Peri-Peri", category = "Appetizers & Chaap", sku = "APP-07", sellingPrice = 160.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Cheese Naan Bomb", category = "Appetizers & Chaap", sku = "APP-08", sellingPrice = 280.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Malai Chaap", category = "Appetizers & Chaap", sku = "CHP-01", sellingPrice = 180.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Achari Chaap", category = "Appetizers & Chaap", sku = "CHP-02", sellingPrice = 190.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Afghani Chaap", category = "Appetizers & Chaap", sku = "CHP-03", sellingPrice = 190.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chatpata Chaap", category = "Appetizers & Chaap", sku = "CHP-04", sellingPrice = 180.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Punjabi Chaap", category = "Appetizers & Chaap", sku = "CHP-05", sellingPrice = 180.0, stockQuantity = 30, kitchenSection = "KITCHEN"),

        // === 3. TANDOOR & KEBABS ===
        ProductItem(name = "Paneer Tikka", category = "Tandoor & Kebabs", sku = "TND-01", sellingPrice = 250.0, stockQuantity = 35, kitchenSection = "GRILL"),
        ProductItem(name = "Paneer Achari Tikka", category = "Tandoor & Kebabs", sku = "TND-02", sellingPrice = 260.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Paneer Afghani Tikka", category = "Tandoor & Kebabs", sku = "TND-03", sellingPrice = 280.0, stockQuantity = 25, kitchenSection = "GRILL"),
        ProductItem(name = "Paneer Malai Tikka", category = "Tandoor & Kebabs", sku = "TND-04", sellingPrice = 280.0, stockQuantity = 25, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Tikka", category = "Tandoor & Kebabs", sku = "TND-05", sellingPrice = 280.0, stockQuantity = 40, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Achari Tikka", category = "Tandoor & Kebabs", sku = "TND-06", sellingPrice = 290.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Afghani Tikka", category = "Tandoor & Kebabs", sku = "TND-07", sellingPrice = 290.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Malai Tikka", category = "Tandoor & Kebabs", sku = "TND-08", sellingPrice = 290.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Tandoori Veg Platter", category = "Tandoor & Kebabs", sku = "TND-09", sellingPrice = 400.0, stockQuantity = 20, kitchenSection = "GRILL"),
        ProductItem(name = "Tandoori Chicken Full", category = "Tandoor & Kebabs", sku = "TND-10", sellingPrice = 540.0, stockQuantity = 25, kitchenSection = "GRILL"),
        ProductItem(name = "Tandoori Chicken Half", category = "Tandoor & Kebabs", sku = "TND-11", sellingPrice = 280.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Tandoori Prawn", category = "Tandoor & Kebabs", sku = "TND-12", sellingPrice = 440.0, stockQuantity = 15, kitchenSection = "GRILL"),
        ProductItem(name = "Fish Tikka", category = "Tandoor & Kebabs", sku = "TND-13", sellingPrice = 320.0, stockQuantity = 20, kitchenSection = "GRILL"),
        ProductItem(name = "Fish Achari Tikka", category = "Tandoor & Kebabs", sku = "TND-14", sellingPrice = 340.0, stockQuantity = 20, kitchenSection = "GRILL"),
        ProductItem(name = "Harabhara Kebab", category = "Tandoor & Kebabs", sku = "KBB-01", sellingPrice = 240.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Veg Shami Kebab", category = "Tandoor & Kebabs", sku = "KBB-02", sellingPrice = 260.0, stockQuantity = 25, kitchenSection = "GRILL"),
        ProductItem(name = "Veg Seekh Kebab", category = "Tandoor & Kebabs", sku = "KBB-03", sellingPrice = 250.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Paneer Seekh Kebab", category = "Tandoor & Kebabs", sku = "KBB-04", sellingPrice = 280.0, stockQuantity = 25, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Seekh Kebab", category = "Tandoor & Kebabs", sku = "KBB-05", sellingPrice = 300.0, stockQuantity = 35, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Haryali Kebab", category = "Tandoor & Kebabs", sku = "KBB-06", sellingPrice = 280.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Boti Kebab", category = "Tandoor & Kebabs", sku = "KBB-07", sellingPrice = 290.0, stockQuantity = 30, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Afghani Kebab", category = "Tandoor & Kebabs", sku = "KBB-08", sellingPrice = 300.0, stockQuantity = 25, kitchenSection = "GRILL"),
        ProductItem(name = "Tangri Kebab (4 piece)", category = "Tandoor & Kebabs", sku = "KBB-09", sellingPrice = 380.0, stockQuantity = 20, kitchenSection = "GRILL"),
        ProductItem(name = "Chicken Banjara Kebab", category = "Tandoor & Kebabs", sku = "KBB-10", sellingPrice = 290.0, stockQuantity = 25, kitchenSection = "GRILL"),
        ProductItem(name = "Fish Fingers", category = "Tandoor & Kebabs", sku = "KBB-11", sellingPrice = 320.0, stockQuantity = 25, kitchenSection = "GRILL"),

        // === 4. CHINESE (VEG & NON-VEG) ===
        ProductItem(name = "Paneer Chilli Dry", category = "Chinese", sku = "CHN-01", sellingPrice = 240.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Chilli Gravy", category = "Chinese", sku = "CHN-02", sellingPrice = 260.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Honey Chilli", category = "Chinese", sku = "CHN-03", sellingPrice = 280.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer 65", category = "Chinese", sku = "CHN-04", sellingPrice = 250.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Salt & Pepper", category = "Chinese", sku = "CHN-05", sellingPrice = 250.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Fried Rice", category = "Chinese", sku = "CHN-06", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Fried Rice", category = "Chinese", sku = "CHN-07", sellingPrice = 160.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Schezwan Fried Rice", category = "Chinese", sku = "CHN-08", sellingPrice = 170.0, stockQuantity = 45, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chilli Garlic Fried Rice", category = "Chinese", sku = "CHN-09", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mix Fried Rice", category = "Chinese", sku = "CHN-10", sellingPrice = 190.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Baby Corn Crispy", category = "Chinese", sku = "CHN-11", sellingPrice = 250.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Baby Corn Chilli Dry", category = "Chinese", sku = "CHN-12", sellingPrice = 240.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Baby Corn Chilli Gravy", category = "Chinese", sku = "CHN-13", sellingPrice = 260.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Chilli Dry", category = "Chinese", sku = "CHN-14", sellingPrice = 250.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Chilli Gravy", category = "Chinese", sku = "CHN-15", sellingPrice = 260.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Honey Chilli", category = "Chinese", sku = "CHN-16", sellingPrice = 290.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Salt & Pepper", category = "Chinese", sku = "CHN-17", sellingPrice = 250.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Szechwan Paneer", category = "Chinese", sku = "CHN-18", sellingPrice = 240.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Manchurian Dry", category = "Chinese", sku = "CHN-19", sellingPrice = 240.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Manchurian Gravy", category = "Chinese", sku = "CHN-20", sellingPrice = 250.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Corn Salt & Pepper", category = "Chinese", sku = "CHN-21", sellingPrice = 230.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Lollipop", category = "Chinese", sku = "CHN-22", sellingPrice = 200.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Honey Chilli Potato", category = "Chinese", sku = "CHN-23", sellingPrice = 210.0, stockQuantity = 45, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Spring Roll", category = "Chinese", sku = "CHN-24", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Chilli Dry", category = "Chinese", sku = "CHN-25", sellingPrice = 260.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Chilli B/L", category = "Chinese", sku = "CHN-26", sellingPrice = 280.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken 65", category = "Chinese", sku = "CHN-27", sellingPrice = 280.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Salt & Pepper", category = "Chinese", sku = "CHN-28", sellingPrice = 290.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Hot Garlic", category = "Chinese", sku = "CHN-29", sellingPrice = 290.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Honey Chilli", category = "Chinese", sku = "CHN-30", sellingPrice = 300.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Lollipop", category = "Chinese", sku = "CHN-31", sellingPrice = 300.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Fried Rice", category = "Chinese", sku = "CHN-32", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Lemon Chicken", category = "Chinese", sku = "CHN-33", sellingPrice = 290.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Szechwan Chicken", category = "Chinese", sku = "CHN-34", sellingPrice = 300.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Spring Roll", category = "Chinese", sku = "CHN-35", sellingPrice = 200.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Schezwan Fried Rice", category = "Chinese", sku = "CHN-36", sellingPrice = 190.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Chilli Garlic Fried Rice", category = "Chinese", sku = "CHN-37", sellingPrice = 200.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Mix Fried Rice", category = "Chinese", sku = "CHN-38", sellingPrice = 210.0, stockQuantity = 30, kitchenSection = "KITCHEN"),

        // === 5. NOODLES ===
        ProductItem(name = "Veg Hakka Noodle", category = "Noodles", sku = "NDL-01", sellingPrice = 160.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Hakka Noodle", category = "Noodles", sku = "NDL-02", sellingPrice = 200.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Baby Corn Hakka Noodle", category = "Noodles", sku = "NDL-03", sellingPrice = 220.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mix Hakka Noodle", category = "Noodles", sku = "NDL-04", sellingPrice = 210.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Hakka Noodle", category = "Noodles", sku = "NDL-05", sellingPrice = 220.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Szechwan Hakka Noodle", category = "Noodles", sku = "NDL-06", sellingPrice = 200.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg American Chopsuey", category = "Noodles", sku = "NDL-07", sellingPrice = 200.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chilli Garlic Hakka Noodle", category = "Noodles", sku = "NDL-08", sellingPrice = 220.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Hakka Noodle", category = "Noodles", sku = "NDL-09", sellingPrice = 260.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mix Hakka Noodle Non-Veg", category = "Noodles", sku = "NDL-10", sellingPrice = 270.0, stockQuantity = 30, kitchenSection = "KITCHEN"),

        // === 6. MAIN-COURSE (VEG & DAL) ===
        ProductItem(name = "Paneer Butter Masala", category = "Main Course (Veg & Dal)", sku = "MCV-01", sellingPrice = 270.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Do Pyaza", category = "Main Course (Veg & Dal)", sku = "MCV-02", sellingPrice = 280.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Kadhai", category = "Main Course (Veg & Dal)", sku = "MCV-03", sellingPrice = 280.0, stockQuantity = 45, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Handi", category = "Main Course (Veg & Dal)", sku = "MCV-04", sellingPrice = 250.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Tikka Butter Masala", category = "Main Course (Veg & Dal)", sku = "MCV-05", sellingPrice = 260.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Afghani Tikka Masala", category = "Main Course (Veg & Dal)", sku = "MCV-06", sellingPrice = 230.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Sahi Paneer", category = "Main Course (Veg & Dal)", sku = "MCV-07", sellingPrice = 200.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Palak Paneer", category = "Main Course (Veg & Dal)", sku = "MCV-08", sellingPrice = 210.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Matar Paneer", category = "Main Course (Veg & Dal)", sku = "MCV-09", sellingPrice = 250.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Malai Kofta", category = "Main Course (Veg & Dal)", sku = "MCV-10", sellingPrice = 290.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Kofta", category = "Main Course (Veg & Dal)", sku = "MCV-11", sellingPrice = 280.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mix Veg", category = "Main Course (Veg & Dal)", sku = "MCV-12", sellingPrice = 250.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Masala", category = "Main Course (Veg & Dal)", sku = "MCV-13", sellingPrice = 240.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Paneer Masala", category = "Main Course (Veg & Dal)", sku = "MCV-14", sellingPrice = 240.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Butter Masala", category = "Main Course (Veg & Dal)", sku = "MCV-15", sellingPrice = 240.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Do Pyaza", category = "Main Course (Veg & Dal)", sku = "MCV-16", sellingPrice = 240.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Mushroom Irani", category = "Main Course (Veg & Dal)", sku = "MCV-17", sellingPrice = 240.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Dal Fry", category = "Main Course (Veg & Dal)", sku = "DAL-01", sellingPrice = 130.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Dal Tadka", category = "Main Course (Veg & Dal)", sku = "DAL-02", sellingPrice = 150.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Dal Makhani", category = "Main Course (Veg & Dal)", sku = "DAL-03", sellingPrice = 200.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Dal Panchratan", category = "Main Course (Veg & Dal)", sku = "DAL-04", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "KITCHEN"),

        // === 7. MAIN-COURSE (NON-VEG) ===
        ProductItem(name = "Egg Masala", category = "Main Course (Non-Veg)", sku = "MCN-01", sellingPrice = 240.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Do Payaza (4pc)", category = "Main Course (Non-Veg)", sku = "MCN-02", sellingPrice = 340.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Butter Masala (4pc)", category = "Main Course (Non-Veg)", sku = "MCN-03", sellingPrice = 360.0, stockQuantity = 45, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Kadhai (4pc)", category = "Main Course (Non-Veg)", sku = "MCN-04", sellingPrice = 340.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Irani (4pc)", category = "Main Course (Non-Veg)", sku = "MCN-05", sellingPrice = 340.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Korma", category = "Main Course (Non-Veg)", sku = "MCN-06", sellingPrice = 360.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Masala (4pc)", category = "Main Course (Non-Veg)", sku = "MCN-07", sellingPrice = 340.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Tikka Masala", category = "Main Course (Non-Veg)", sku = "MCN-08", sellingPrice = 280.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Afghani Tikka Masala", category = "Main Course (Non-Veg)", sku = "MCN-09", sellingPrice = 380.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Desi Chicken", category = "Main Course (Non-Veg)", sku = "MCN-10", sellingPrice = 1000.0, stockQuantity = 15, kitchenSection = "KITCHEN"),
        ProductItem(name = "Murg Mussalam (8pc)", category = "Main Course (Non-Veg)", sku = "MCN-11", sellingPrice = 800.0, stockQuantity = 15, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Kassa (4pc)", category = "Main Course (Non-Veg)", sku = "MCN-12", sellingPrice = 380.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Dehati [8pc / Full]", category = "Main Course (Non-Veg)", sku = "MCN-13", sellingPrice = 800.0, stockQuantity = 20, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Dehati [Half]", category = "Main Course (Non-Veg)", sku = "MCN-14", sellingPrice = 250.0, stockQuantity = 25, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Handi [8pc / Full]", category = "Main Course (Non-Veg)", sku = "MCN-15", sellingPrice = 800.0, stockQuantity = 20, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Handi [Half]", category = "Main Course (Non-Veg)", sku = "MCN-16", sellingPrice = 230.0, stockQuantity = 25, kitchenSection = "KITCHEN"),

        // === 8. RICE & BIRYANI ===
        ProductItem(name = "Plain Rice", category = "Rice & Biryani", sku = "RIC-01", sellingPrice = 100.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Jeera Rice", category = "Rice & Biryani", sku = "RIC-02", sellingPrice = 130.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Pulao", category = "Rice & Biryani", sku = "RIC-03", sellingPrice = 150.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Pulao", category = "Rice & Biryani", sku = "RIC-04", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Peas Pulao", category = "Rice & Biryani", sku = "RIC-05", sellingPrice = 140.0, stockQuantity = 35, kitchenSection = "KITCHEN"),
        ProductItem(name = "Kashmiri Pulao", category = "Rice & Biryani", sku = "RIC-06", sellingPrice = 200.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Biryani", category = "Rice & Biryani", sku = "BRY-01", sellingPrice = 180.0, stockQuantity = 45, kitchenSection = "KITCHEN"),
        ProductItem(name = "Chicken Biryani", category = "Rice & Biryani", sku = "BRY-02", sellingPrice = 240.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Hyderabadi Chicken Biryani", category = "Rice & Biryani", sku = "BRY-03", sellingPrice = 250.0, stockQuantity = 40, kitchenSection = "KITCHEN"),

        // === 9. INDIAN BREAD ===
        ProductItem(name = "Tandoori Roti", category = "Indian Bread", sku = "BRD-01", sellingPrice = 20.0, stockQuantity = 200, kitchenSection = "KITCHEN"),
        ProductItem(name = "Tandoori Butter Roti", category = "Indian Bread", sku = "BRD-02", sellingPrice = 25.0, stockQuantity = 200, kitchenSection = "KITCHEN"),
        ProductItem(name = "Naan", category = "Indian Bread", sku = "BRD-03", sellingPrice = 35.0, stockQuantity = 150, kitchenSection = "KITCHEN"),
        ProductItem(name = "Butter Naan", category = "Indian Bread", sku = "BRD-04", sellingPrice = 40.0, stockQuantity = 180, kitchenSection = "KITCHEN"),
        ProductItem(name = "Garlic Naan", category = "Indian Bread", sku = "BRD-05", sellingPrice = 60.0, stockQuantity = 100, kitchenSection = "KITCHEN"),
        ProductItem(name = "Cheese Naan", category = "Indian Bread", sku = "BRD-06", sellingPrice = 90.0, stockQuantity = 80, kitchenSection = "KITCHEN"),
        ProductItem(name = "Plain Kulcha", category = "Indian Bread", sku = "BRD-07", sellingPrice = 50.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Masala Kulcha", category = "Indian Bread", sku = "BRD-08", sellingPrice = 60.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Paneer Kulcha", category = "Indian Bread", sku = "BRD-09", sellingPrice = 80.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Amritsar Kulcha", category = "Indian Bread", sku = "BRD-10", sellingPrice = 90.0, stockQuantity = 50, kitchenSection = "KITCHEN"),

        // === 10. PAPAD, SALAD & RAITA ===
        ProductItem(name = "Dry Papad", category = "Papad, Salad & Raita", sku = "PAP-01", sellingPrice = 20.0, stockQuantity = 100, kitchenSection = "KITCHEN"),
        ProductItem(name = "Fry Papad", category = "Papad, Salad & Raita", sku = "PAP-02", sellingPrice = 30.0, stockQuantity = 100, kitchenSection = "KITCHEN"),
        ProductItem(name = "Masala Papad", category = "Papad, Salad & Raita", sku = "PAP-03", sellingPrice = 80.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Green Salad", category = "Papad, Salad & Raita", sku = "SLD-01", sellingPrice = 100.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Onion Salad", category = "Papad, Salad & Raita", sku = "SLD-02", sellingPrice = 80.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Fruit Salad", category = "Papad, Salad & Raita", sku = "SLD-03", sellingPrice = 180.0, stockQuantity = 30, kitchenSection = "KITCHEN"),
        ProductItem(name = "Veg Raita", category = "Papad, Salad & Raita", sku = "RTA-01", sellingPrice = 90.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Onion Raita", category = "Papad, Salad & Raita", sku = "RTA-02", sellingPrice = 90.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Boondi Raita", category = "Papad, Salad & Raita", sku = "RTA-03", sellingPrice = 90.0, stockQuantity = 40, kitchenSection = "KITCHEN"),
        ProductItem(name = "Pineapple Raita", category = "Papad, Salad & Raita", sku = "RTA-04", sellingPrice = 120.0, stockQuantity = 35, kitchenSection = "KITCHEN"),

        // === 11. SPECIAL MOCKTAILS ===
        ProductItem(name = "Virgin Mojito", category = "Special Mocktails", sku = "MCK-01", sellingPrice = 170.0, stockQuantity = 50, kitchenSection = "BAR"),
        ProductItem(name = "Virgin Kiwi Cooler", category = "Special Mocktails", sku = "MCK-02", sellingPrice = 170.0, stockQuantity = 45, kitchenSection = "BAR"),
        ProductItem(name = "Mumbai Maharani", category = "Special Mocktails", sku = "MCK-03", sellingPrice = 150.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Indian Summer", category = "Special Mocktails", sku = "MCK-04", sellingPrice = 170.0, stockQuantity = 45, kitchenSection = "BAR"),
        ProductItem(name = "Adam Eve", category = "Special Mocktails", sku = "MCK-05", sellingPrice = 160.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Patyala the Maharaja", category = "Special Mocktails", sku = "MCK-06", sellingPrice = 165.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Sweet Sixteen", category = "Special Mocktails", sku = "MCK-07", sellingPrice = 165.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Apple Eye", category = "Special Mocktails", sku = "MCK-08", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "BAR"),
        ProductItem(name = "Fruit Punch", category = "Special Mocktails", sku = "MCK-09", sellingPrice = 150.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Mango Boom", category = "Special Mocktails", sku = "MCK-10", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "BAR"),
        ProductItem(name = "Cinderella", category = "Special Mocktails", sku = "MCK-11", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "BAR"),
        ProductItem(name = "Virgin Pinacolada", category = "Special Mocktails", sku = "MCK-12", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "BAR"),
        ProductItem(name = "Lipstick on the Collar", category = "Special Mocktails", sku = "MCK-13", sellingPrice = 170.0, stockQuantity = 30, kitchenSection = "BAR"),
        ProductItem(name = "First Impression", category = "Special Mocktails", sku = "MCK-14", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "BAR"),
        ProductItem(name = "Vanilla Ice Cream with Orange Syrup", category = "Special Mocktails", sku = "MCK-15", sellingPrice = 170.0, stockQuantity = 30, kitchenSection = "BAR"),
        ProductItem(name = "Shirley Temple", category = "Special Mocktails", sku = "MCK-16", sellingPrice = 170.0, stockQuantity = 35, kitchenSection = "BAR"),

        // === 12. BEVERAGES & SHAKES ===
        ProductItem(name = "Coffee", category = "Beverages & Shakes", sku = "BEV-01", sellingPrice = 140.0, stockQuantity = 60, kitchenSection = "BAR"),
        ProductItem(name = "Cold Coffee", category = "Beverages & Shakes", sku = "BEV-02", sellingPrice = 160.0, stockQuantity = 50, kitchenSection = "BAR"),
        ProductItem(name = "Coffee with Ice-Cream", category = "Beverages & Shakes", sku = "BEV-03", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Masala Cold Drink", category = "Beverages & Shakes", sku = "BEV-04", sellingPrice = 120.0, stockQuantity = 60, kitchenSection = "BAR"),
        ProductItem(name = "Redbull", category = "Beverages & Shakes", sku = "BEV-05", sellingPrice = 200.0, stockQuantity = 45, kitchenSection = "BAR"),
        ProductItem(name = "Monster", category = "Beverages & Shakes", sku = "BEV-06", sellingPrice = 200.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Can Cold Drink", category = "Beverages & Shakes", sku = "BEV-07", sellingPrice = 110.0, stockQuantity = 80, kitchenSection = "BAR"),
        ProductItem(name = "Soda (750ml)", category = "Beverages & Shakes", sku = "BEV-08", sellingPrice = 80.0, stockQuantity = 70, kitchenSection = "BAR"),
        ProductItem(name = "Vanilla Shake", category = "Beverages & Shakes", sku = "SHK-01", sellingPrice = 140.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Mango Shake", category = "Beverages & Shakes", sku = "SHK-02", sellingPrice = 160.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Kesar Pista Shake", category = "Beverages & Shakes", sku = "SHK-03", sellingPrice = 180.0, stockQuantity = 35, kitchenSection = "BAR"),
        ProductItem(name = "Oreo Shake", category = "Beverages & Shakes", sku = "SHK-04", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Brownie Shake", category = "Beverages & Shakes", sku = "SHK-05", sellingPrice = 200.0, stockQuantity = 35, kitchenSection = "BAR"),
        ProductItem(name = "Kitkat Shake", category = "Beverages & Shakes", sku = "SHK-06", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "BAR"),
        ProductItem(name = "Chocolate Shake", category = "Beverages & Shakes", sku = "SHK-07", sellingPrice = 180.0, stockQuantity = 40, kitchenSection = "BAR"),

        // === 13. DESSERTS ===
        ProductItem(name = "Gulab Jamun (2 pcs)", category = "Desserts", sku = "DES-01", sellingPrice = 80.0, stockQuantity = 50, kitchenSection = "KITCHEN"),
        ProductItem(name = "Ice-Cream", category = "Desserts", sku = "DES-02", sellingPrice = 100.0, stockQuantity = 60, kitchenSection = "KITCHEN"),
        ProductItem(name = "Ice-Cream with Gulab Jamun", category = "Desserts", sku = "DES-03", sellingPrice = 160.0, stockQuantity = 40, kitchenSection = "KITCHEN")
    )

    // === 3 TABLE SECTIONS: Club area, Outdoor, Back area ===
    val sampleTables = listOf(
        // Section 1: Club area
        RestaurantTable(
            tableNumber = "C-1",
            zone = "Club area",
            capacity = 4,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("C-1", "Club area")
        ),
        RestaurantTable(
            tableNumber = "C-2",
            zone = "Club area",
            capacity = 6,
            status = "AVAILABLE",
            currentGuestName = "",
            currentBillAmount = 0.0,
            qrPayload = TableUrlGenerator.createDynamicTableUrl("C-2", "Club area")
        ),
        RestaurantTable(
            tableNumber = "C-3",
            zone = "Club area",
            capacity = 8,
            status = "AVAILABLE",
            currentGuestName = "",
            currentBillAmount = 0.0,
            qrPayload = TableUrlGenerator.createDynamicTableUrl("C-3", "Club area")
        ),
        RestaurantTable(
            tableNumber = "C-4",
            zone = "Club area",
            capacity = 4,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("C-4", "Club area")
        ),
        RestaurantTable(
            tableNumber = "C-5",
            zone = "Club area",
            capacity = 6,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("C-5", "Club area")
        ),
        RestaurantTable(
            tableNumber = "C-6",
            zone = "Club area",
            capacity = 10,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("C-6", "Club area")
        ),

        // Section 2: Outdoor
        RestaurantTable(
            tableNumber = "O-1",
            zone = "Outdoor",
            capacity = 2,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("O-1", "Outdoor")
        ),
        RestaurantTable(
            tableNumber = "O-2",
            zone = "Outdoor",
            capacity = 4,
            status = "AVAILABLE",
            currentGuestName = "",
            currentBillAmount = 0.0,
            qrPayload = TableUrlGenerator.createDynamicTableUrl("O-2", "Outdoor")
        ),
        RestaurantTable(
            tableNumber = "O-3",
            zone = "Outdoor",
            capacity = 4,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("O-3", "Outdoor")
        ),
        RestaurantTable(
            tableNumber = "O-4",
            zone = "Outdoor",
            capacity = 6,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("O-4", "Outdoor")
        ),
        RestaurantTable(
            tableNumber = "O-5",
            zone = "Outdoor",
            capacity = 4,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("O-5", "Outdoor")
        ),
        RestaurantTable(
            tableNumber = "O-6",
            zone = "Outdoor",
            capacity = 8,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("O-6", "Outdoor")
        ),

        // Section 3: Back area
        RestaurantTable(
            tableNumber = "B-1",
            zone = "Back area",
            capacity = 4,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("B-1", "Back area")
        ),
        RestaurantTable(
            tableNumber = "B-2",
            zone = "Back area",
            capacity = 4,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("B-2", "Back area")
        ),
        RestaurantTable(
            tableNumber = "B-3",
            zone = "Back area",
            capacity = 6,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("B-3", "Back area")
        ),
        RestaurantTable(
            tableNumber = "B-4",
            zone = "Back area",
            capacity = 4,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("B-4", "Back area")
        ),
        RestaurantTable(
            tableNumber = "B-5",
            zone = "Back area",
            capacity = 6,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("B-5", "Back area")
        ),
        RestaurantTable(
            tableNumber = "B-6",
            zone = "Back area",
            capacity = 8,
            status = "AVAILABLE",
            qrPayload = TableUrlGenerator.createDynamicTableUrl("B-6", "Back area")
        )
    )

    suspend fun seedIfNeeded(dao: PosDao) = seedDatabase(dao)

    suspend fun seedDatabase(dao: PosDao) {
        if (dao.checkRestaurantProductsSeeded() == 0) {
            dao.deleteAllProducts()
            dao.insertAllProducts(sampleRestaurantProducts)
        }

        if (dao.getTableByNumber("C-1") == null) {
            dao.deleteAllTables()
            dao.insertAllTables(sampleTables)
        } else {
            dao.clearAllRunningTables()
        }

        if (dao.getKotCount() == 0) {
            val now = System.currentTimeMillis()
            dao.insertAllKots(
                listOf(
                    KitchenOrderTicket(
                        kotNumber = "KOT-101",
                        tableNumber = "C-2",
                        zone = "Club area",
                        customerName = "Aman & Friends",
                        timestamp = now - (8 * 60 * 1000),
                        status = "PREPARING",
                        section = "KITCHEN",
                        itemsSummary = "1x Paneer Chilli Dry ; 2x Butter Naan ; 1x Dal Makhani",
                        completedItems = "1x Paneer Chilli Dry",
                        specialNotes = "Less spicy for Dal Makhani. Extra butter on Naan."
                    ),
                    KitchenOrderTicket(
                        kotNumber = "KOT-102",
                        tableNumber = "O-2",
                        zone = "Outdoor",
                        customerName = "Vikram",
                        timestamp = now - (4 * 60 * 1000),
                        status = "READY",
                        section = "BAR",
                        itemsSummary = "1x Virgin Mojito ; 1x Oreo Shake",
                        completedItems = "1x Virgin Mojito ; 1x Oreo Shake",
                        specialNotes = "Serve with ice cubes and paper straws."
                    ),
                    KitchenOrderTicket(
                        kotNumber = "KOT-103",
                        tableNumber = "C-3",
                        zone = "Club area",
                        customerName = "Rohan Party",
                        timestamp = now - (2 * 60 * 1000),
                        status = "NEW",
                        section = "GRILL",
                        itemsSummary = "1x Chicken Tikka ; 1x Tandoori Chicken Half ; 1x Harabhara Kebab",
                        completedItems = "",
                        specialNotes = "Crispy grill, serve mint chutney."
                    )
                )
            )
        }

        if (dao.getOrderCount() == 0) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            cal.timeInMillis = now
            cal.add(Calendar.HOUR_OF_DAY, -1)

            val order1Id = dao.insertOrder(
                SaleOrder(
                    invoiceNumber = "INV-2026-0001",
                    timestamp = cal.timeInMillis,
                    customerName = "Table C-2",
                    customerPhone = "+91 9876543210",
                    paymentMethod = "ONLINE",
                    subtotal = 1380.0,
                    discountPercent = 0.0,
                    discountAmount = 0.0,
                    taxAmount = 69.0,
                    totalAmount = 1449.0,
                    totalCost = 550.0,
                    netProfit = 830.0,
                    tableNumber = "C-2",
                    orderType = "DINE_IN",
                    notes = "UPI Payment successful (Ref: UPI-98421)"
                )
            )

            dao.insertOrderItems(
                listOf(
                    SaleOrderItem(
                        orderId = order1Id,
                        productId = 1,
                        productName = "Paneer Chilli Dry",
                        category = "Chinese",
                        quantity = 1,
                        unitPrice = 240.0,
                        costPrice = 90.0,
                        gstRate = 5.0,
                        taxAmount = 12.0,
                        totalAmount = 252.0
                    ),
                    SaleOrderItem(
                        orderId = order1Id,
                        productId = 2,
                        productName = "Chicken Biryani",
                        category = "Rice & Biryani",
                        quantity = 2,
                        unitPrice = 240.0,
                        costPrice = 110.0,
                        gstRate = 5.0,
                        taxAmount = 24.0,
                        totalAmount = 504.0
                    ),
                    SaleOrderItem(
                        orderId = order1Id,
                        productId = 3,
                        productName = "Virgin Mojito",
                        category = "Special Mocktails",
                        quantity = 2,
                        unitPrice = 170.0,
                        costPrice = 60.0,
                        gstRate = 5.0,
                        taxAmount = 17.0,
                        totalAmount = 357.0
                    )
                )
            )
        }
    }
}
