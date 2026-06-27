package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.db.GamepadDatabase
import com.example.data.repository.GamepadRepository
import com.example.ui.GamepadApp
import com.example.ui.GamepadViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database, DAO and Repository
        val database = GamepadDatabase.getDatabase(this)
        val repository = GamepadRepository(database.gamepadDao())

        // Create ViewModel using custom factory
        val viewModel = GamepadViewModel(
            context = applicationContext,
            repository = repository
        )

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GamepadApp(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
