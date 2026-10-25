package com.planningpoker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.planningpoker.core.designsystem.theme.PlanningPokerTheme
import com.planningpoker.ui.PlanningPokerApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlanningPokerTheme {
                PlanningPokerApp()
            }
        }
    }
}
