package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.ComparisonRepository
import com.example.ui.prompt.PromptComparerScreen
import com.example.ui.prompt.PromptViewModel
import com.example.ui.prompt.PromptViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Database & Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ComparisonRepository(database.comparisonDao())
    
    // Initialize ViewModel
    val viewModel = ViewModelProvider(this, PromptViewModelFactory(repository))[PromptViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          PromptComparerScreen(viewModel = viewModel)
        }
      }
    }
  }
}

