package com.app.gdl.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.gdl.domain.model.CustomerEntity
import com.app.gdl.utils.AppData
import com.app.gdl.utils.Converters

@Database(entities = [CustomerEntity :: class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase  : RoomDatabase() {

    abstract fun userDao(): CustomerDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java,
                    AppData.database
                ).build()
                INSTANCE = instance
                instance
            }

        }
    }
}
