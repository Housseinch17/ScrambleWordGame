package com.example.scramblewordgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scramblewordgame.ui.screens.HomeScreen
import com.example.scramblewordgame.ui.theme.ScrambleWordGameTheme
import com.example.scramblewordgame.ui.viewModels.ScrambleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScrambleWordGameTheme {
                val scrambleViewModel: ScrambleViewModel = viewModel()
                HomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    viewModel = scrambleViewModel,
                )
            }
        }
    }
}





