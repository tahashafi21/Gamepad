package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ControllerButton
import com.example.data.model.SettingsProfile

@Database(entities = [ControllerButton::class, SettingsProfile::class], version = 1, exportSchema = false)
abstract class GamepadDatabase : RoomDatabase() {
    abstract fun gamepadDao(): GamepadDao

    companion object {
        @Volatile
        private var INSTANCE: GamepadDatabase? = null

        fun getDatabase(context: Context): GamepadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GamepadDatabase::class.java,
                    "gamepad_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
