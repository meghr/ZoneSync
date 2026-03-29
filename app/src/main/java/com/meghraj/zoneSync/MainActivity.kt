package com.meghraj.zoneSync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.meghraj.zoneSync.ui.screens.MainScreen
import com.meghraj.zoneSync.ui.theme.ZoneSyncTheme
import com.meghraj.zoneSync.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZoneSyncTheme {
                MainScreen(viewModel)
            }
        }
    }
}