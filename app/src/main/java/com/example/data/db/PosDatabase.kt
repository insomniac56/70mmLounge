package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.KitchenOrderTicket
import com.example.data.model.ProductItem
import com.example.data.model.RestaurantTable
import com.example.data.model.SaleOrder
import com.example.data.model.SaleOrderItem

@Database(
    entities = [
        ProductItem::class,
        RestaurantTable::class,
        KitchenOrderTicket::class,
        SaleOrder::class,
        SaleOrderItem::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {

    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: PosDatabase? = null

        fun getDatabase(context: Context): PosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    "lounge70mm_pos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
