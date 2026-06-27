package com.example.data.db

import androidx.room.*
import com.example.data.model.ControllerButton
import com.example.data.model.SettingsProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface GamepadDao {
    @Query("SELECT * FROM controller_buttons")
    fun getAllButtons(): Flow<List<ControllerButton>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: ControllerButton)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButtons(buttons: List<ControllerButton>)

    @Update
    suspend fun updateButton(button: ControllerButton)

    @Delete
    suspend fun deleteButton(button: ControllerButton)

    @Query("DELETE FROM controller_buttons")
    suspend fun clearAllButtons()

    @Query("SELECT * FROM settings_profile WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsProfile)
}
